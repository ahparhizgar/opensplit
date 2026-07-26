package com.opensplit.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.ExpenseDao
import com.opensplit.db.ExpenseEntity
import com.opensplit.db.OperationType
import com.opensplit.db.SyncQueueDao
import com.opensplit.db.SyncQueueEntity
import com.opensplit.db.toDto
import com.opensplit.db.toEntity
import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.util.currentTimeMillis
import com.opensplit.util.randomId
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ExpenseRepository(
    private val api: ExpenseApi,
    private val expenseDao: ExpenseDao,
    private val syncQueueDao: SyncQueueDao,
    private val database: AppDatabase,
) {

  fun getExpenses(householdId: String): Flow<List<ExpenseDto>> {
    return expenseDao.getExpenses(householdId).map { entities ->
      entities.map { entity ->
        // TODO should we optimize this 1+n query?
        val participants = expenseDao.getParticipants(entity.id).map { it.toDto() }
        entity.toDto(participants)
      }
    }
  }

  // TODO Should we only get unsynced expenses?
  suspend fun refreshExpenses(householdId: String) {
    val result = api.getExpenses(householdId)
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        result.forEach { dto ->
          val entity = dto.toEntity(SyncStatus.SYNCED)
          val participants = dto.participants.map { p -> p.toEntity(dto.id) }
          expenseDao.insertExpenseWithParticipants(entity, participants)
          syncQueueDao.removeByEntityId(dto.id)
        }
      }
    }
  }

  suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
      payerId: String,
      participants: List<ParticipantShareDto>,
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
    // TODO why to save two rows, when the expense entry has all we need to know?
    val payload =
        CreateExpenseRequest(
            title = title,
            amount = amount,
            participants = participants,
            splitMethod = splitMethod,
        )

    val syncEntry =
        SyncQueueEntity(
            operation = OperationType.CREATE_EXPENSE,
            entityId = expenseId,
            payloadJson = Json.encodeToString(payload),
            createdAt = now.toEpochMilliseconds(),
        )

    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        expenseDao.insertExpenseWithParticipants(expenseEntity, participantEntities)
        syncQueueDao.enqueue(syncEntry)
      }
    }
  }
}
