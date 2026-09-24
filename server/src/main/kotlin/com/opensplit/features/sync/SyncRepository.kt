package com.opensplit.features.sync

import com.opensplit.dto.sync.SyncResponse
import kotlinx.serialization.Serializable

interface SyncRepository {
  fun recordChange(entityType: SyncEntityType, entityId: String, operation: SyncOperation): Long

  fun getChanges(sinceVersion: Long, userId: String): SyncResponse
}

@Serializable
enum class SyncEntityType {
  EXPENSE,
  MEMBERSHIP,
}

@Serializable
enum class SyncOperation {
  INSERT,
  UPDATE,
  DELETE,
}
