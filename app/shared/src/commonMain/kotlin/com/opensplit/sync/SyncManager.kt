package com.opensplit.sync

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.OperationType
import com.opensplit.db.ParticipantEntity
import com.opensplit.db.SyncMetadataEntity
import com.opensplit.db.SyncQueueEntity
import com.opensplit.db.toDto
import com.opensplit.db.toEntity
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.sync.SyncResponse
import com.opensplit.features.expense.ExpenseApi
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class SyncManager(
    private val expenseApi: ExpenseApi,
    private val syncApi: SyncApi,
    private val database: AppDatabase,
    defaultDispatcher: CoroutineDispatcher,
) {
  private val scope = CoroutineScope(SupervisorJob() + defaultDispatcher)
  private val syncQueueDao = database.syncQueueDao()
  private val expenseDao = database.expenseDao()
  private val syncMetadataDao = database.syncMetadataDao()
  private val householdDao = database.householdDao()
  private val syncMutex = Mutex()

  fun triggerSync() = scope.launch { sync() }

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
              // Reconcile balance in case server adjusted shares
              val oldParticipants = expenseDao.getParticipants(entry.entityId)
              updateBalances(expense.householdId, oldParticipants, result.shares)

              expenseDao.deleteExpense(entry.entityId)
              expenseDao.deleteParticipants(entry.entityId)

              val serverEntity = result.toEntity(SyncStatus.SYNCED)
              val serverParticipants = result.shares.map { it.toEntity(result.id) }
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

  private suspend fun applyChanges(response: SyncResponse) {
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        response.changedEntities.expenses.forEach { dto ->
          val oldParticipants = expenseDao.getParticipants(dto.id)
          updateBalances(dto.householdId, oldParticipants, dto.shares)

          val entity = dto.toEntity(SyncStatus.SYNCED)
          val participants = dto.shares.map { it.toEntity(dto.id) }
          expenseDao.insertExpenseWithParticipants(entity, participants)
        }

        response.deletedEntities.expenses.forEach { id ->
          val expense = expenseDao.getExpense(id) ?: return@forEach
          val oldParticipants = expenseDao.getParticipants(id)
          updateBalances(expense.householdId, oldParticipants, emptyList())

          expenseDao.deleteExpense(id)
          expenseDao.deleteParticipants(id)
        }

        syncMetadataDao.insertMetadata(
            SyncMetadataEntity("last_sync_version", response.latestVersion.toString())
        )
      }
    }
  }

  private suspend fun updateBalances(
      householdId: String,
      oldParticipants: List<ParticipantEntity>,
      newShares: List<ParticipantShareDto>,
  ) {
    val memberDeltas = mutableMapOf<String, Double>()

    // Subtract old impact
    oldParticipants.forEach { p ->
      val impact = p.paidShare - p.consumedShare
      memberDeltas[p.userId] = (memberDeltas[p.userId] ?: 0.0) - impact
    }

    // Add new impact
    newShares.forEach { p ->
      val impact = p.paidShare - p.consumedShare
      memberDeltas[p.userId] = (memberDeltas[p.userId] ?: 0.0) + impact
    }

    // Apply deltas to DB
    memberDeltas.forEach { (userId, delta) ->
      if (delta != 0.0) {
        householdDao.updateMemberBalance(householdId, userId, delta)
      }
    }
  }
}

interface SyncDaemon {
  fun start()
}

class DefaultSyncDaemon(
    private val syncManager: SyncManager,
    defaultDispatcher: CoroutineDispatcher,
) : SyncDaemon {
  private val scope = CoroutineScope(defaultDispatcher)

  override fun start() {
    scope.launch {
      while (isActive) {
        syncManager.sync()
        delay(30.seconds) // Longer delay for background sync
      }
    }
  }
}

class NoopSyncDaemon() : SyncDaemon {
  override fun start() {}
}
