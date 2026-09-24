package com.opensplit.dto.sync

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.FakeExpenseDtoFactory
import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse(
    val latestVersion: Long,
    val changedEntities: ChangedEntitiesDto,
    val deletedEntities: DeletedEntitiesDto,
)

object FakeSyncResponseFactory {
  fun create(
      latestVersion: Long = 1L,
      changedEntities: ChangedEntitiesDto = FakeChangedEntitiesDtoFactory.create(),
      deletedEntities: DeletedEntitiesDto = FakeDeletedEntitiesDtoFactory.create(),
  ) =
      SyncResponse(
          latestVersion = latestVersion,
          changedEntities = changedEntities,
          deletedEntities = deletedEntities,
      )
}

@Serializable
data class ChangedEntitiesDto(
    val expenses: List<ExpenseDto> = emptyList(),
)

object FakeChangedEntitiesDtoFactory {
  fun create(expenses: List<ExpenseDto> = FakeExpenseDtoFactory.createList()) =
      ChangedEntitiesDto(expenses = expenses)
}

@Serializable
data class DeletedEntitiesDto(
    val expenses: List<String> = emptyList(),
)

object FakeDeletedEntitiesDtoFactory {
  fun create(expenses: List<String> = emptyList()) = DeletedEntitiesDto(expenses = expenses)
}
