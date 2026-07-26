package com.opensplit.sync

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.ExpenseDao
import com.opensplit.db.OperationType
import com.opensplit.db.SyncQueueDao
import com.opensplit.db.SyncQueueEntity
import com.opensplit.db.toEntity
import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.features.expense.ExpenseApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SyncManager(
    private val expenseApi: ExpenseApi,
    private val syncQueueDao: SyncQueueDao,
    private val expenseDao: ExpenseDao,
    private val database: AppDatabase,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var syncJob: Job? = null

  fun startSync() {
    if (syncJob?.isActive == true) return
    syncJob = scope.launch {
      while (isActive) {
        try {
          val queue = syncQueueDao.getQueue().first()
          if (queue.isNotEmpty()) {
            processQueue(queue)
          }
        } catch (_: Exception) {
          // ignore
        }
        delay(5000)
      }
    }
  }

  private suspend fun processQueue(queue: List<SyncQueueEntity>) {
    for (entry in queue) {
      try {
        when (entry.operation) {
          OperationType.CREATE_EXPENSE -> {
            val payload = Json.decodeFromString<CreateExpenseRequest>(entry.payloadJson)
            val localExpense = expenseDao.getExpense(entry.entityId)
            if (localExpense != null) {
              val result =
                  expenseApi.createExpense(
                      householdId = localExpense.householdId,
                      title = payload.title,
                      amount = payload.amount,
                      participants = payload.participants,
                      splitMethod = payload.splitMethod,
                  )

              database.useWriterConnection { connection ->
                connection.immediateTransaction {
                  expenseDao.deleteExpense(localExpense.id)
                  expenseDao.deleteParticipants(localExpense.id)

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
          else -> {}
        }
      } catch (_: Exception) {
        // skip
      }
    }
  }
}
