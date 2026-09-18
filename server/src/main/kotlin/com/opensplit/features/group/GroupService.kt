package com.opensplit.features.group

import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.GroupMemberDto
import com.opensplit.features.auth.UserPrincipal

class GroupService(private val groupRepository: GroupRepository) {
  fun loadGroups(user: UserPrincipal): List<GroupDto> =
      groupRepository.loadGroups(user.userId).map { it.toDto(user.userId) }

  fun createGroup(user: UserPrincipal, name: String): GroupDto {
    val group = groupRepository.createGroup(name, user.userId)
    return GroupDto(
        id = group.id,
        name = group.name,
        members =
            listOf(
                GroupMemberDto(
                    userId = user.userId,
                    name = user.name,
                    email = user.email,
                    isOwner = true,
                    isCurrentUser = true,
                    balance = 0.0,
                )
            ),
        inviteLink = group.inviteLink(),
        isOwner = true,
    )
  }

  fun joinGroup(user: UserPrincipal, inviteCodeOrIdOrLink: String): JoinGroupResult {
    val inviteCode = inviteCodeOrIdOrLink.removePrefix("https://opensplit.com/join/")
    val groupByInvite = groupRepository.findGroupByInviteCode(inviteCode)
    val group = groupByInvite ?: groupRepository.findGroupById(inviteCodeOrIdOrLink)
    if (group == null) {
      return JoinGroupResult.InvalidInviteCode
    }

    if (
        groupByInvite == null &&
            group.ownerId != user.userId &&
            !groupRepository.hasMembership(group.id, user.userId)
    ) {
      return JoinGroupResult.MissingPermission
    }

    groupRepository.ensureMembership(group.id, user.userId)
    val detail =
        groupRepository.loadGroupDetail(group.id, user.userId)
            ?: return JoinGroupResult.InvalidInviteCode
    return JoinGroupResult.Success(detail.toDto(user.userId))
  }

  fun addMemberByEmail(
      user: UserPrincipal,
      groupId: String,
      email: String,
  ): AddMemberByEmailResult {
    val group =
        groupRepository.findGroupById(groupId) ?: return AddMemberByEmailResult.GroupNotFound
    if (group.ownerId != user.userId) {
      return AddMemberByEmailResult.Forbidden
    }

    val targetUser =
        groupRepository.findMemberByEmail(email)
            ?: return AddMemberByEmailResult.UserNotFound(email)

    groupRepository.ensureMembership(groupId, targetUser.userId)
    val detail =
        groupRepository.loadGroupDetail(groupId, user.userId)
            ?: return AddMemberByEmailResult.GroupNotFound
    return AddMemberByEmailResult.Success(detail.toDto(user.userId))
  }

  fun leaveGroup(user: UserPrincipal, groupId: String): List<GroupDto> {
    groupRepository.leaveGroup(groupId, user.userId)
    return loadGroups(user)
  }

  fun getGroup(user: UserPrincipal, groupId: String): GroupDto? =
      groupRepository.loadGroupDetail(groupId, user.userId)?.toDto(user.userId)

  private fun GroupDetailRecord.toDto(currentUserId: String): GroupDto {
    val memberDtos = members.map { member ->
      GroupMemberDto(
          userId = member.userId,
          name = member.name,
          email = member.email,
          isOwner = member.userId == group.ownerId,
          isCurrentUser = member.userId == currentUserId,
          balance = member.balance,
      )
    }
    return GroupDto(
        id = group.id,
        name = group.name,
        members = memberDtos,
        inviteLink = group.inviteLink(),
        isOwner = group.ownerId == currentUserId,
    )
  }

  private fun GroupRecord.inviteLink(): String =
      "https://opensplit.com/join/${inviteCode.orEmpty()}"
}
