package com.opensplit.features.sync

import com.opensplit.dto.sync.SyncResponse

interface SyncRepository {
  fun recordChange(entityType: String, entityId: String, operation: String): Long

  fun getChanges(sinceVersion: Long, userId: String): SyncResponse
}
