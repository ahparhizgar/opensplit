package com.opensplit.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.ExpenseDao
import com.opensplit.db.ExpenseEntity
import com.opensplit.db.OperationType
import com.opensplit.db.SyncQueueDao
import com.opensplit.db.SyncQueueEntity
import com.opensplit.db.toDomain
import com.opensplit.db.toEntity
import com.opensplit.domain.Expense
import com.opensplit.domain.ParticipantShare
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.sync.SyncManager
import com.opensplit.util.currentTimeMillis
import com.opensplit.util.randomId
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val syncQueueDao: SyncQueueDao,
    private val database: AppDatabase,
    private val syncManager: SyncManager,
) {

  fun getExpenses(householdId: String): Flow<List<Expense>> {
    return expenseDao.getExpenses(householdId).map { entities ->
      entities.map { entity ->
        // TODO should we optimize this 1+n query?
        val participants = expenseDao.getParticipants(entity.id).map { it.toDomain() }
        entity.toDomain(participants)
      }
    }
  }

  fun getExpense(expenseId: String): Flow<Expense?> {
    return expenseDao.observeExpense(expenseId).map { entity ->
      entity?.let {
        val participants = expenseDao.getParticipants(it.id).map { it.toDomain() }
        it.toDomain(participants)
      }
    }
  }

  suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
      payerId: String,
      participants: List<ParticipantShare>,
      splitMethod: SplitMethod,
  ) {
    val expenseId = "local_" + randomId()
    val now = Instant.fromEpochMilliseconds(currentTimeMillis())

    val expenseEntity =
        ExpenseEntity(
            id = expenseId,
            householdId = householdId,
            title = title,
            amount = amount,
            payerId = payerId,
            createdAtEpochMillis = now.toEpochMilliseconds(),
            splitMethodJson = Json.encodeToString(splitMethod),
            syncStatus = SyncStatus.PENDING,
        )

    val participantEntities = participants.map { it.toEntity(expenseId) }

    val syncEntry =
        SyncQueueEntity(
            operation = OperationType.CREATE,
            entityType = "EXPENSE",
            entityId = expenseId,
            createdAt = now.toEpochMilliseconds(),
        )

    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        expenseDao.insertExpenseWithParticipants(expenseEntity, participantEntities)
        syncQueueDao.enqueue(syncEntry)
      }
    }
    syncManager.triggerSync()
  }

  suspend fun deleteExpense(householdId: String, expenseId: String) {
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        expenseDao.deleteExpense(expenseId)
        expenseDao.deleteParticipants(expenseId)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                operation = OperationType.DELETE,
                entityType = "EXPENSE",
                entityId = expenseId,
                metadata = householdId,
                createdAt = currentTimeMillis(),
            )
        )
      }
    }
    syncManager.triggerSync()
  }
}
