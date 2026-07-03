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
data class HouseholdSummaryDto(
    val id: String,
    val name: String,
    val memberCount: Int,
    val isOwner: Boolean = false,
    val inviteCode: String? = null,
    val balance: Double = 0.0,
    val currency: String = "IRR",
    val isSettled: Boolean = false,
    val description: String? = null,
)

@Serializable
data class HouseholdOverviewDto(
    val households: List<HouseholdSummaryDto> = emptyList(),
    val members: List<HouseholdMemberDto> = emptyList(),
    val overallBalance: Double = 0.0,
    val overallCurrency: String = "IRR",
)

