package com.opensplit.fake

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.sync.ChangedEntitiesDto
import com.opensplit.dto.sync.DeletedEntitiesDto
import com.opensplit.dto.sync.SyncResponse
import com.opensplit.sync.SyncApi
import com.opensplit.util.FakeService

class FakeSyncApi : SyncApi, FakeService {
  override var errorToThrow: Exception? = null
  var expenses = emptyList<ExpenseDto>()

  override suspend fun getChanges(sinceVersion: Long): SyncResponse = fakeApiCall {
    SyncResponse(
        latestVersion = sinceVersion,
        changedEntities = ChangedEntitiesDto(expenses = expenses),
        deletedEntities = DeletedEntitiesDto(),
    )
  }
}
