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
data class HouseholdDto(
    val id: String,
    val name: String,
    val members: List<HouseholdMemberDto>,
    val isOwner: Boolean = false,
    val inviteLink: String,
    val balance: Double = 0.0,
) {
  val isSettled: Boolean
    get() = balance == 0.0
}

object FakeHouseholdDtoFactory {
  fun create(
      id: String = "household-1",
      name: String = "My Household",
      members: List<HouseholdMemberDto> = FakeHouseholdMemberDtoFactory.createList(),
  ) =
      HouseholdDto(
          id = id,
          name = name,
          members = members,
          inviteLink = "https://opensplit.com/invite/85243892",
      )
}
