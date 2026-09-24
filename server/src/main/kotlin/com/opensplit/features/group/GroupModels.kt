package com.opensplit.features.group

import com.opensplit.dto.group.GroupDto
import kotlin.time.Instant

data class GroupRecord(
    val id: String,
    val name: String,
    val ownerId: String,
    val inviteCode: String?,
    val lastInteractionAt: Instant = Instant.DISTANT_PAST,
)

data class GroupMemberRecord(
    val userId: String,
    val name: String,
    val email: String,
    val balance: Double = 0.0,
)

data class GroupDetailRecord(
    val group: GroupRecord,
    val members: List<GroupMemberRecord>,
)

sealed interface JoinGroupResult {
  data class Success(val group: GroupDto) : JoinGroupResult

  data object InvalidInviteCode : JoinGroupResult

  data object MissingPermission : JoinGroupResult
}

sealed interface AddMemberByEmailResult {
  data class Success(val group: GroupDto) : AddMemberByEmailResult

  data object GroupNotFound : AddMemberByEmailResult

  data object Forbidden : AddMemberByEmailResult

  data class UserNotFound(val email: String) : AddMemberByEmailResult
}
