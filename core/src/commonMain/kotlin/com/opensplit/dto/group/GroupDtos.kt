package com.opensplit.dto.group

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
)

@Serializable
data class JoinGroupRequest(
    val inviteCodeOrIdOrLink: String,
)

@Serializable
data class AddMemberByEmailRequest(
    val email: String,
)

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val members: List<GroupMemberDto>,
    val isOwner: Boolean = false,
    val inviteLink: String,
)

object FakeGroupDtoFactory {
  fun create(
      id: String = "group-1",
      name: String = "My Group",
      members: List<GroupMemberDto> = FakeGroupMemberDtoFactory.createList(),
  ) =
      GroupDto(
          id = id,
          name = name,
          members = members,
          inviteLink = "https://opensplit.com/invite/85243892",
      )
}
