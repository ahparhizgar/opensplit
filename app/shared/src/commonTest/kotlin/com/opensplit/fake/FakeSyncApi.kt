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
  var deletedExpenseIds = emptyList<String>()
  var latestVersion = 1L

  override suspend fun getChanges(sinceVersion: Long): SyncResponse = fakeApiCall {
    SyncResponse(
        latestVersion = latestVersion,
        changedEntities = ChangedEntitiesDto(expenses = expenses),
        deletedEntities = DeletedEntitiesDto(expenses = deletedExpenseIds),
    )
  }
}
