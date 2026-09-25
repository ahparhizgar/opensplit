package com.opensplit.integration.group

interface GroupRepository {
  fun loadGroups(userId: String): List<GroupDetailRecord>

  fun createGroup(name: String, ownerId: String): GroupRecord

  fun findGroupByInviteCode(inviteCode: String): GroupRecord?

  fun findGroupById(groupId: String): GroupRecord?

  fun hasMembership(groupId: String, userId: String): Boolean

  fun ensureMembership(groupId: String, userId: String)

  fun findMemberByEmail(email: String): GroupMemberRecord?

  fun loadGroupDetail(groupId: String, currentUserId: String): GroupDetailRecord?

  fun leaveGroup(groupId: String, userId: String)
}
