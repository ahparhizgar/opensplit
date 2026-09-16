package com.opensplit.dto.household

import kotlinx.serialization.Serializable

@Serializable
data class HouseholdMemberDto(
    val userId: String,
    val name: String,
    val email: String,
    val isOwner: Boolean = false,
    val isCurrentUser: Boolean = false,
    val balance: Double = 0.0,
)

object FakeHouseholdMemberDtoFactory {
  fun create(
      id: String = "user-id",
      name: String = "User $id",
      balance: Double = 0.0,
      isCurrentUser: Boolean = false,
  ) =
      HouseholdMemberDto(
          userId = id,
          name = name,
          email = "$id@example.com",
          balance = balance,
          isCurrentUser = isCurrentUser,
      )

  fun create1() = create("user-1", "Amir Hossein Parhizgar", 0.0, isCurrentUser = true)

  fun create2() = create("user-2", "Abolqasem Ferdowsi", 0.0, isCurrentUser = false)

  fun createList() = listOf(create1(), create2())
}
