package com.opensplit.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.HouseholdDao
import com.opensplit.db.HouseholdEntity
import com.opensplit.db.OperationType
import com.opensplit.db.SyncQueueEntity
import com.opensplit.db.toDto
import com.opensplit.db.toEntity
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.features.household.HouseholdApi
import com.opensplit.util.currentTimeMillis
import com.opensplit.util.randomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HouseholdRepository(
    private val api: HouseholdApi,
    private val dao: HouseholdDao,
    private val database: AppDatabase,
    private val syncManager: com.opensplit.sync.SyncManager,
) {
  fun getHouseholds(): Flow<List<HouseholdDto>> {
    return dao.getHouseholds().map { entities -> entities.map { it.toDto() } }
  }

  suspend fun refreshHouseholds() {
    val result = api.loadOverview()
    dao.insertHouseholds(result.map { it.toEntity() })
  }

  suspend fun getHousehold(id: String): HouseholdDto? {
    return dao.getHousehold(id)?.toDto()
  }

  suspend fun refreshHousehold(id: String): HouseholdDto {
    val result = api.getHousehold(id)
    dao.insertHouseholds(listOf(result.toEntity()))
    return result
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
        dao.insertHouseholds(listOf(entity))
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
        dao.deleteById(householdId)
        database.syncQueueDao().enqueue(syncEntry)
      }
    }
    syncManager.triggerSync()
  }
}
