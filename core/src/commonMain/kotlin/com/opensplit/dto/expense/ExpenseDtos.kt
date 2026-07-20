package com.opensplit.dto.expense

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantShareDto(
    val userId: String,
    val paidShare: Double,
    val owedShare: Double,
    val netBalance: Double,
)

@Serializable
data class ExpenseDto(
    val id: String,
    val householdId: String,
    val title: String,
    val amount: Double,
    val payerId: String,
    val createdAt: Instant,
    val participants: List<ParticipantShareDto> = emptyList(),
)

@Serializable
data class CreateExpenseRequest(
    val title: String,
    val amount: Double,
    val participants: List<ParticipantShareDto>,
)

@Serializable
enum class SplitType {
  EQUALLY,
  EXACT,
  PERCENTAGE,
  SHARES,
  ADJUSTMENT,
}
