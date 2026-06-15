package com.opensplit.dto.household

import kotlinx.serialization.Serializable

@Serializable
data class CreateHouseholdRequest(
    val name: String,
)

@Serializable
data class CreateHouseholdResponse(
    val id: String,
    val name: String,
    val inviteCode: String? = null,
)

@Serializable
data class JoinHouseholdRequest(
    val inviteCodeOrId: String,
)

@Serializable
data class JoinHouseholdResponse(
    val householdId: String,
    val joined: Boolean,
)

@Serializable
data class SwitchHouseholdRequest(
    val householdId: String,
)

@Serializable
data class HouseholdSummaryResponse(
    val id: String,
    val name: String,
    val memberCount: Int,
    val isOwner: Boolean = false,
    val inviteCode: String? = null,
)

@Serializable
data class HouseholdMemberResponse(
    val userId: String,
    val email: String,
    val isOwner: Boolean = false,
    val isCurrentUser: Boolean = false,
)

@Serializable
data class HouseholdOverviewResponse(
    val households: List<HouseholdSummaryResponse> = emptyList(),
    val members: List<HouseholdMemberResponse> = emptyList(),
)

