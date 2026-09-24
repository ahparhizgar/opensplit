package com.opensplit.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Group(
    val id: String,
    val name: String,
    val members: List<Member>,
    val isOwner: Boolean = false,
    val inviteLink: String,
    val balance: Double = 0.0,
    val lastInteractionAt: Instant = Instant.DISTANT_PAST,
) {
  val isSettled: Boolean
    get() = balance == 0.0
}

@Serializable
data class Member(
    val userId: String,
    val name: String,
    val email: String,
    val isOwner: Boolean = false,
    val isCurrentUser: Boolean = false,
    val balance: Double = 0.0,
)

object FakeGroupFactory {
  fun create(
      id: String = "group-1",
      name: String = "My Group",
      members: List<Member> = FakeMemberFactory.createList(),
      isOwner: Boolean = false,
      inviteLink: String = "https://opensplit.com/invite/85243892",
      balance: Double = 0.0,
  ) =
      Group(
          id = id,
          name = name,
          members = members,
          isOwner = isOwner,
          inviteLink = inviteLink,
          balance = balance,
      )
}
