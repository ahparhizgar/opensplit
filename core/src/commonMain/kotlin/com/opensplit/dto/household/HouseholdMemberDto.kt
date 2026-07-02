package com.opensplit.dto.household

import kotlinx.serialization.Serializable

@Serializable
data class HouseholdMemberDto(
    val userId: String,
    val name: String? = null,
    val email: String,
    val isOwner: Boolean = false,
    val isCurrentUser: Boolean = false,
    val balance: Double = 0.0,
    val balanceCurrency: String = "IRR",
)

object FakeHouseholdMemberDtoFactory {
    fun create(
        id: String = "user-id",
        name: String = "User $id",
        balance: Double = 0.0,
        isCurrentUser: Boolean = false
    ) =
        HouseholdMemberDto(
            userId = id,
            name = name,
            email = "$id@example.com",
            balance = balance,
            isCurrentUser = isCurrentUser
        )

    fun create1() = create("user-1", "AmirHossein (you)", 10.15, true)
    fun create2() = create("user-2", "Ali Bagherifam", -10.15)
    fun createList() = listOf(create1(), create2())
}
