package com.opensplit.features.sync

import com.opensplit.dto.sync.SyncResponse
import com.opensplit.features.auth.UserPrincipal

class SyncService(private val syncRepository: SyncRepository) {
  fun getChanges(sinceVersion: Long, user: UserPrincipal): SyncResponse {
    return syncRepository.getChanges(sinceVersion, user.userId)
  }
}
