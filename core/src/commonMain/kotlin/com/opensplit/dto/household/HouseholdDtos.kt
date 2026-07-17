package com.opensplit.dto.household

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateHouseholdRequest(
    val name: String,
)

@Serializable
sealed interface JoinHouseholdRequest {
  @Serializable
  @SerialName("invite")
  data class ByInvite(val inviteCodeOrIdOrLink: String) : JoinHouseholdRequest

  @Serializable
  @SerialName("email")
  data class ByEmail(val email: String, val householdId: String) : JoinHouseholdRequest
}

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
