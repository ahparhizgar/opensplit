package com.opensplit.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.HouseholdDao
import com.opensplit.db.toDomain
import com.opensplit.db.toEntity
import com.opensplit.domain.Household
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.features.household.HouseholdApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HouseholdRepository(
    private val api: HouseholdApi,
    private val dao: HouseholdDao,
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
   * Saves household from server but preserves local balance calculation. Local transactions are the
   * source of truth for balances.
   */
  private suspend fun saveHouseholdWithPendingAdjustment(dto: HouseholdDto) {
    val existingMembers =
        dao.getHouseholdWithMembers(dto.id)?.members?.associateBy { it.userId } ?: emptyMap()
    val existingHousehold = dao.getHousehold(dto.id)

    val adjustedHouseholdBalance = existingHousehold?.balance ?: dto.balance
    val adjustedMemberBalances =
        dto.members.associate { m -> m.userId to (existingMembers[m.userId]?.balance ?: 0.0) }

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
