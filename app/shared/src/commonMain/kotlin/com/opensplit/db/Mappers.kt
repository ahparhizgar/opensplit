package com.opensplit.db

import com.opensplit.domain.Expense
import com.opensplit.domain.Group
import com.opensplit.domain.Member
import com.opensplit.domain.ParticipantShare
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.GroupMemberDto
import kotlinx.serialization.json.Json

fun GroupDto.toEntity() =
    GroupEntity(
        id = id,
        name = name,
        inviteLink = inviteLink,
        isOwner = isOwner,
    )

fun GroupEntity.toDto(members: List<GroupMemberDto>) =
    GroupDto(
        id = id,
        name = name,
        members = members,
        inviteLink = inviteLink,
        isOwner = isOwner,
    )

fun GroupWithMembers.toDto() = group.toDto(members.map { it.toDto() })

fun GroupWithMembers.toDomain(): Group {
  val memberList = members.map { it.toDomain() }
  return Group(
      id = group.id,
      name = group.name,
      members = memberList,
      isOwner = group.isOwner,
      inviteLink = group.inviteLink,
      balance = memberList.find { it.isCurrentUser }?.balance ?: 0.0,
  )
}

fun GroupDto.toDomain(): Group {
  val memberList = members.map { it.toDomain() }
  return Group(
      id = id,
      name = name,
      members = memberList,
      isOwner = isOwner,
      inviteLink = inviteLink,
      balance = memberList.find { it.isCurrentUser }?.balance ?: 0.0,
  )
}

fun GroupMemberDto.toEntity(groupId: String) =
    MemberEntity(
        groupId = groupId,
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
        balance = balance,
    )

fun MemberEntity.toDto() =
    GroupMemberDto(
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
    )

fun MemberEntity.toDomain() =
    Member(
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
        balance = balance,
    )

fun GroupMemberDto.toDomain() =
    Member(
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
        balance = balance,
    )

fun ExpenseDto.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED) =
    ExpenseEntity(
        id = id,
        groupId = groupId,
        title = title,
        amount = amount,
        creator = creator,
        createdAtEpochMillis = createdAt.toEpochMilliseconds(),
        splitMethodJson = Json.encodeToString(splitMethod),
        syncStatus = syncStatus,
    )

fun ExpenseEntity.toDto(participants: List<ParticipantShareDto>) =
    ExpenseDto(
        id = id,
        groupId = groupId,
        title = title,
        amount = amount,
        creator = creator,
        createdAt = kotlin.time.Instant.fromEpochMilliseconds(createdAtEpochMillis),
        shares = participants,
        splitMethod = Json.decodeFromString(splitMethodJson),
        syncStatus = syncStatus,
    )

fun ExpenseEntity.toDomain(participants: List<ParticipantShare>) =
    Expense(
        id = id,
        groupId = groupId,
        title = title,
        amount = amount,
        creator = creator,
        createdAt = kotlin.time.Instant.fromEpochMilliseconds(createdAtEpochMillis),
        participants = participants,
        splitMethod = Json.decodeFromString(splitMethodJson),
        syncStatus = syncStatus,
    )

fun ExpenseDto.toDomain() =
    Expense(
        id = id,
        groupId = groupId,
        title = title,
        amount = amount,
        creator = creator,
        createdAt = createdAt,
        participants = shares.map { it.toDomain() },
        splitMethod = splitMethod,
        syncStatus = syncStatus,
    )

fun ParticipantShare.toEntity(expenseId: String) =
    ParticipantEntity(
        expenseId = expenseId,
        userId = userId,
        paidShare = paidShare,
        consumedShare = consumedShare,
    )

fun ParticipantShareDto.toEntity(expenseId: String) =
    ParticipantEntity(
        expenseId = expenseId,
        userId = userId,
        paidShare = paidShare,
        consumedShare = consumedShare,
    )

fun ParticipantEntity.toDto() =
    ParticipantShareDto(
        userId = userId,
        paidShare = paidShare,
        consumedShare = consumedShare,
    )

fun ParticipantEntity.toDomain() =
    ParticipantShare(
        userId = userId,
        paidShare = paidShare,
        consumedShare = consumedShare,
    )

fun ParticipantShareDto.toDomain() =
    ParticipantShare(
        userId = userId,
        paidShare = paidShare,
        consumedShare = consumedShare,
    )
