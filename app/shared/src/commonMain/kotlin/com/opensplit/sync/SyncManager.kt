package com.opensplit.sync

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.*
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.sync.SyncResponse
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.features.household.HouseholdApi
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class SyncManager(
    private val expenseApi: ExpenseApi,
    private val householdApi: HouseholdApi,
    private val syncApi: SyncApi,
    private val database: AppDatabase,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val syncQueueDao = database.syncQueueDao()
  private val expenseDao = database.expenseDao()
  private val householdDao = database.householdDao()
  private val syncMetadataDao = database.syncMetadataDao()
  private val syncMutex = Mutex()

  fun startSync() {
    scope.launch {
      while (isActive) {
        sync()
        delay(30.seconds) // Longer delay for background sync
      }
    }
  }

  fun triggerSync() {
    scope.launch { sync() }
  }

  suspend fun sync() {
    if (syncMutex.isLocked) return
    syncMutex.withLock {
      try {
        processOutbox()
        val lastVersion = syncMetadataDao.getMetadata("last_sync_version")?.toLong() ?: 0L
        val response = syncApi.getChanges(lastVersion)
        applyChanges(response)
      } catch (_: Exception) {
        // ignore
      }
    }
  }

  private suspend fun processOutbox() {
    val queue = syncQueueDao.getQueue().first()
    for (entry in queue) {
      try {
        when (entry.entityType) {
          "EXPENSE" -> processExpenseOperation(entry)
          "HOUSEHOLD" -> processHouseholdOperation(entry)
        }
      } catch (_: Exception) {
        // skip and retry later
      }
    }
  }

  private suspend fun processExpenseOperation(entry: SyncQueueEntity) {
    val expense = expenseDao.getExpense(entry.entityId)
    val participants = expenseDao.getParticipants(entry.entityId)

    when (entry.operation) {
      OperationType.CREATE -> {
        if (expense != null) {
          val result =
              expenseApi.createExpense(
                  householdId = expense.householdId,
                  title = expense.title,
                  amount = expense.amount,
                  payerId = expense.payerId,
                  participants = participants.map { it.toDto() },
                  splitMethod = Json.decodeFromString(expense.splitMethodJson),
              )

          database.useWriterConnection { connection ->
            connection.immediateTransaction {
              expenseDao.deleteExpense(entry.entityId)
              expenseDao.deleteParticipants(entry.entityId)

              val serverEntity = result.toEntity(SyncStatus.SYNCED)
              val serverParticipants = result.participants.map { it.toEntity(result.id) }
              expenseDao.insertExpenseWithParticipants(serverEntity, serverParticipants)

              syncQueueDao.dequeue(entry)
            }
          }
        } else {
          syncQueueDao.dequeue(entry)
        }
      }
      OperationType.DELETE -> {
        val householdId = entry.metadata
        if (householdId != null) {
          expenseApi.deleteExpense(householdId, entry.entityId)
        }
        syncQueueDao.dequeue(entry)
      }
      else -> {}
    }
  }

  private suspend fun processHouseholdOperation(entry: SyncQueueEntity) {
    val household = householdDao.getHousehold(entry.entityId)
    when (entry.operation) {
      OperationType.CREATE -> {
        if (household != null) {
          val result = householdApi.createHousehold(household.name)
          database.useWriterConnection { connection ->
            connection.immediateTransaction {
              householdDao.deleteHousehold(entry.entityId)
              householdDao.insertHouseholdWithMembers(
                  result.toEntity(),
                  result.members.map { it.toEntity(result.id) },
              )
              syncQueueDao.dequeue(entry)
            }
          }
        } else {
          syncQueueDao.dequeue(entry)
        }
      }
      OperationType.DELETE -> {
        householdApi.leaveHousehold(entry.entityId)
        syncQueueDao.dequeue(entry)
      }
      else -> {}
    }
  }

  private suspend fun applyChanges(response: SyncResponse) {
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        response.changedEntities.expenses.forEach { dto ->
          val entity = dto.toEntity(SyncStatus.SYNCED)
          val participants = dto.participants.map { it.toEntity(dto.id) }
          expenseDao.insertExpenseWithParticipants(entity, participants)
        }

        response.deletedEntities.expenses.forEach { id ->
          expenseDao.deleteExpense(id)
          expenseDao.deleteParticipants(id)
        }

        syncMetadataDao.insertMetadata(
            SyncMetadataEntity("last_sync_version", response.latestVersion.toString())
        )
      }
    }
  }
}
