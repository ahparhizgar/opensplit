package com.opensplit.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.ExpenseDao
import com.opensplit.db.HouseholdDao
import com.opensplit.db.OperationType
import com.opensplit.db.toDomain
import com.opensplit.db.toEntity
import com.opensplit.domain.Household
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.features.household.HouseholdApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HouseholdRepository(
    private val api: HouseholdApi,
    private val dao: HouseholdDao,
    private val expenseDao: ExpenseDao,
    private val profileRepository: ProfileRepository,
    private val database: AppDatabase,
    private val scope: CoroutineScope,
) {
  fun getHouseholds(): Flow<List<Household>> {
    return dao.getHouseholdsWithMembers().map { entities -> entities.map { it.toDomain() } }
  }

  fun observeHousehold(id: String): Flow<Household?> {
    return dao.observeHouseholdWithMembers(id).map { it?.toDomain() }
  }

  fun refresh() = scope.launch {
    try {
      val result = api.getHouseholds()
      database.useWriterConnection { connection ->
        connection.immediateTransaction {
          result.forEach { dto -> saveHouseholdWithPendingAdjustment(dto) }
        }
      }
    } catch (_: Exception) {
      // Ignore errors for background refresh in offline-first
    }
  }

  suspend fun getHousehold(id: String): Household? {
    return dao.getHouseholdWithMembers(id)?.toDomain()
  }

  /**
   * Saves household from server but re-applies any pending local expense math. This prevents
   * "balance flickering" while sync is in progress.
   */
  private suspend fun saveHouseholdWithPendingAdjustment(dto: HouseholdDto) {
    val pendingExpenses =
        database.syncQueueDao().getQueue().first().filter {
          it.entityType == "EXPENSE" && it.operation == OperationType.CREATE
        }

    var adjustedHouseholdBalance = dto.balance
    val adjustedMemberBalances = dto.members.associate { it.userId to it.balance }.toMutableMap()
    val currentUserId = profileRepository.profile.value?.id

    pendingExpenses.forEach { entry ->
      val expense = expenseDao.getExpense(entry.entityId)
      if (expense != null && expense.householdId == dto.id) {
        val participants = expenseDao.getParticipants(entry.entityId)
        participants.forEach { p ->
          val delta = p.paidShare - p.owedShare
          adjustedMemberBalances[p.userId] = (adjustedMemberBalances[p.userId] ?: 0.0) + delta
          if (p.userId == currentUserId) {
            adjustedHouseholdBalance += delta
          }
        }
      }
    }

    val householdEntity = dto.toEntity().copy(balance = adjustedHouseholdBalance)
    val memberEntities =
        dto.members.map { m ->
          m.toEntity(dto.id).copy(balance = adjustedMemberBalances[m.userId] ?: m.balance)
        }

    dao.insertHouseholdWithMembers(householdEntity, memberEntities)
  }

  suspend fun leaveHousehold(householdId: String) {
    api.leaveHousehold(householdId)
    database.useWriterConnection { connection ->
      connection.immediateTransaction { dao.deleteHousehold(householdId) }
    }
  }

  suspend fun createHousehold(name: String): Household {
    val result = api.createHousehold(name)
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        dao.insertHouseholdWithMembers(
            result.toEntity(),
            result.members.map { it.toEntity(result.id) },
        )
      }
    }
    return result.toDomain()
  }

  suspend fun joinHousehold(inviteCode: String): Household {
    val result = api.joinHousehold(inviteCode)
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        dao.insertHouseholdWithMembers(
            result.toEntity(),
            result.members.map { it.toEntity(result.id) },
        )
      }
    }
    return result.toDomain()
  }
}
