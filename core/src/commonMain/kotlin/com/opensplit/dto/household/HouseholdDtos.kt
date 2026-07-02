package com.opensplit.dto.household

import kotlinx.serialization.Serializable

@Serializable
data class CreateHouseholdRequest(
    val name: String,
)

// To be replaced with household overview dto
@Serializable
data class NewHouseholdDto(
    val id: String,
    val name: String,
    val inviteCode: String? = null,
)

@Serializable
data class JoinHouseholdRequest(
    val inviteCodeOrId: String,
)


@Serializable
data class HouseholdSummaryDto(
    val id: String,
    val name: String,
    val memberCount: Int,
    val isOwner: Boolean = false,
    val inviteCode: String? = null,
)

@Serializable
data class HouseholdMemberDto(
    val userId: String,
    val email: String,
    val isOwner: Boolean = false,
    val isCurrentUser: Boolean = false,
)

@Serializable
data class HouseholdOverviewDto(
    val households: List<HouseholdSummaryDto> = emptyList(),
    val members: List<HouseholdMemberDto> = emptyList(),
)
