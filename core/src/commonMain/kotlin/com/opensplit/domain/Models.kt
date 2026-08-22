package com.opensplit.domain

import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val userId: String,
    val name: String,
    val email: String,
    val isOwner: Boolean = false,
    val isCurrentUser: Boolean = false,
    val balance: Double = 0.0,
)

@Serializable
data class Household(
    val id: String,
    val name: String,
    val members: List<Member>,
    val isOwner: Boolean = false,
    val inviteLink: String,
    val balance: Double = 0.0,
) {
  val isSettled: Boolean
    get() = balance == 0.0
}

@Serializable
data class ParticipantShare(
    val userId: String,
    val paidShare: Double,
    val consumedShare: Double,
) {
  val netBalance: Double
    get() = paidShare - consumedShare
}

@Serializable
data class Expense(
    val id: String,
    val householdId: String,
    val title: String,
    val amount: Double,
    val creator: String,
    val createdAt: Instant,
    val participants: List<ParticipantShare>,
    val splitMethod: SplitMethod,
    val syncStatus: SyncStatus,
)
