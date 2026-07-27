package com.opensplit.dto.sync

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.household.HouseholdDto
import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse(
    val latestVersion: Long,
    val changedEntities: ChangedEntitiesDto,
    val deletedEntities: DeletedEntitiesDto,
)

@Serializable
data class ChangedEntitiesDto(
    val households: List<HouseholdDto> = emptyList(),
    val expenses: List<ExpenseDto> = emptyList(),
)

@Serializable
data class DeletedEntitiesDto(
    val households: List<String> = emptyList(),
    val expenses: List<String> = emptyList(),
)
