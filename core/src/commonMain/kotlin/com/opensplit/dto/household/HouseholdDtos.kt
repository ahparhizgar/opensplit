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
