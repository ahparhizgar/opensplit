package com.opensplit.dto.household

import kotlinx.serialization.Serializable

@Serializable
data class HouseholdMemberDto(
    val userId: String,
    val email: String,
    val isOwner: Boolean = false,
    val isCurrentUser: Boolean = false,
)

object FakeHouseholdMemberDtoFactory {
    fun create(id: String = "user-id") =
        HouseholdMemberDto(
            userId = id,
            email = "$id@example.com"
        )

    fun create1() = create("user-1")
    fun create2() = create("user-2")
    fun createList() = listOf(create1(), create2())
}