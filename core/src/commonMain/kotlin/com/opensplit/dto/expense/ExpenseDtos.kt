package com.opensplit.dto.expense

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantShareDto(
    val userId: String,
    val paidShare: Double,
    val consumedShare: Double,
)

@Serializable
data class ExpenseDto(
    val id: String,
    val householdId: String,
    val title: String,
    val amount: Double,
    val payerId: String,
    val createdAt: Instant,
    val shares: List<ParticipantShareDto> = emptyList(),
    val splitMethod: SplitMethod,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

@Serializable
enum class SyncStatus {
  SYNCED,
  PENDING,
}

@Serializable
data class CreateExpenseRequest(
    val title: String,
    val amount: Double,
    val payerId: String,
    val participants: List<ParticipantShareDto>,
    val splitMethod: SplitMethod,
)

@Serializable
enum class SplitType {
  EQUALLY,
  Unequally,
  PERCENTAGE,
  SHARES,
  ADJUSTMENT,
}
