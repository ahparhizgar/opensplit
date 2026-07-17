package com.opensplit.dto.household

import kotlinx.serialization.Serializable

@Serializable
data class CreateHouseholdRequest(
    val name: String,
)

@Serializable
data class JoinHouseholdRequest(
    val inviteCodeOrIdOrLink: String,
)

@Serializable
data class AddMemberByEmailRequest(
    val email: String,
)

@Serializable
data class HouseholdSummaryDto(
    val id: String,
    val name: String,
    val memberCount: Int,
    val isOwner: Boolean = false,
    val inviteCode: String? = null,
    val balance: Double = 0.0,
) {
  val isSettled: Boolean
    get() = balance == 0.0
}
