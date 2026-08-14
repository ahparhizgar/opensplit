package com.opensplit.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.ExpenseDao
import com.opensplit.db.ExpenseEntity
import com.opensplit.db.HouseholdDao
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
    private val householdDao: HouseholdDao,
    private val syncQueueDao: SyncQueueDao,
    private val profileRepository: ProfileRepository,
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
      shares: List<ParticipantShare>,
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

    val participantEntities = shares.map { it.toEntity(expenseId) }

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

        // Optimistic UI: Update local household/member balances
        val currentUserId = profileRepository.profile.value?.id
        shares.forEach { participant ->
          val delta = participant.paidShare - participant.consumedShare
          householdDao.updateMemberBalance(householdId, participant.userId, delta)

          if (participant.userId == currentUserId) {
            householdDao.updateBalance(householdId, delta)
          }
        }

        syncQueueDao.enqueue(syncEntry)
      }
    }
    syncManager.triggerSync()
  }

  suspend fun deleteExpense(householdId: String, expenseId: String) {
    val participants = expenseDao.getParticipants(expenseId)

    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        // Reverse optimistic UI balance
        val currentUserId = profileRepository.profile.value?.id
        participants.forEach { participant ->
          val delta = participant.paidShare - participant.consumedShare
          householdDao.updateMemberBalance(householdId, participant.userId, -delta)

          if (participant.userId == currentUserId) {
            householdDao.updateBalance(householdId, -delta)
          }
        }

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
