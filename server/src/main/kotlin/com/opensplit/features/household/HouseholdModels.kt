package com.opensplit.features.household

import com.opensplit.dto.household.HouseholdDto

data class HouseholdRecord(
    val id: String,
    val name: String,
    val ownerId: String,
    val inviteCode: String?,
)

data class HouseholdMemberRecord(
    val userId: String,
    val name: String?,
    val email: String,
)

data class HouseholdDetailRecord(
    val household: HouseholdRecord,
    val members: List<HouseholdMemberRecord>,
)

sealed interface JoinHouseholdResult {
  data class Success(val household: HouseholdDto) : JoinHouseholdResult

  data object InvalidInviteCode : JoinHouseholdResult

  data object MissingPermission : JoinHouseholdResult
}

sealed interface AddMemberByEmailResult {
  data class Success(val household: HouseholdDto) : AddMemberByEmailResult

  data object HouseholdNotFound : AddMemberByEmailResult

  data object Forbidden : AddMemberByEmailResult

  data class UserNotFound(val email: String) : AddMemberByEmailResult
}
