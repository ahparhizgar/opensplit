package com.opensplit.dto.household

import kotlinx.serialization.Serializable

@Serializable
data class HouseholdDto(
    val id: String,
    val name: String,
    val members: List<HouseholdMemberDto>,
    val inviteLink: String,
)

object FakeHouseholdDtoFactory {
    fun create(
        id: String = "household-1",
        name: String = "My Household",
        members: List<HouseholdMemberDto> = FakeHouseholdMemberDtoFactory.createList(),
    ) = HouseholdDto(
        id = id,
        name = name,
        members = members,
        inviteLink = "https://opensplit.com/invite/85243892"
    )
}
