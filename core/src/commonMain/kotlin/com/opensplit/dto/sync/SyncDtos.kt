package com.opensplit.dto.sync

import com.opensplit.dto.expense.ExpenseDto
import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse(
    val latestVersion: Long,
    val changedEntities: ChangedEntitiesDto,
    val deletedEntities: DeletedEntitiesDto,
)

@Serializable
data class ChangedEntitiesDto(
    val expenses: List<ExpenseDto> = emptyList(),
)

@Serializable
data class DeletedEntitiesDto(
    val expenses: List<String> = emptyList(),
)
