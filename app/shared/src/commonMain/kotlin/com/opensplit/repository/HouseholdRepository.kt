package com.opensplit.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.HouseholdDao
import com.opensplit.db.HouseholdEntity
import com.opensplit.db.OperationType
import com.opensplit.db.SyncQueueEntity
import com.opensplit.db.toDomain
import com.opensplit.db.toEntity
import com.opensplit.domain.Household
import com.opensplit.features.household.HouseholdApi
import com.opensplit.sync.SyncManager
import com.opensplit.util.currentTimeMillis
import com.opensplit.util.randomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HouseholdRepository(
    private val api: HouseholdApi,
    private val dao: HouseholdDao,
    private val database: AppDatabase,
    private val syncManager: SyncManager,
) {
  fun getHouseholds(): Flow<List<Household>> {
    return dao.getHouseholdsWithMembers().map { entities -> entities.map { it.toDomain() } }
  }

  suspend fun refreshHouseholds() {
    val result = api.loadOverview()
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        result.forEach { dto ->
          dao.insertHouseholdWithMembers(dto.toEntity(), dto.members.map { it.toEntity(dto.id) })
        }
      }
    }
  }

  suspend fun getHousehold(id: String): Household? {
    return dao.getHouseholdWithMembers(id)?.toDomain()
  }

  suspend fun refreshHousehold(id: String): Household {
    val result = api.getHousehold(id)
    dao.insertHouseholdWithMembers(result.toEntity(), result.members.map { it.toEntity(result.id) })
    return result.toDomain()
  }

  suspend fun createHousehold(name: String) {
    val id = "local_" + randomId()
    val entity =
        HouseholdEntity(
            id = id,
            name = name,
            inviteLink = "",
            balance = 0.0,
            isOwner = true,
        )

    val syncEntry =
        SyncQueueEntity(
            operation = OperationType.CREATE,
            entityType = "HOUSEHOLD",
            entityId = id,
            createdAt = currentTimeMillis(),
        )

    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        dao.insertHouseholdWithMembers(entity, emptyList())
        database.syncQueueDao().enqueue(syncEntry)
      }
    }
    syncManager.triggerSync()
  }

  suspend fun leaveHousehold(householdId: String) {
    val syncEntry =
        SyncQueueEntity(
            operation = OperationType.DELETE,
            entityType = "HOUSEHOLD",
            entityId = householdId,
            createdAt = currentTimeMillis(),
        )

    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        dao.deleteHousehold(householdId)
        database.syncQueueDao().enqueue(syncEntry)
      }
    }
    syncManager.triggerSync()
  }
}
