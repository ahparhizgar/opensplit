package com.opensplit.db

import com.opensplit.domain.Expense
import com.opensplit.domain.Household
import com.opensplit.domain.Member
import com.opensplit.domain.ParticipantShare
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import kotlinx.serialization.json.Json

fun HouseholdDto.toEntity() =
    HouseholdEntity(
        id = id,
        name = name,
        inviteLink = inviteLink,
        isOwner = isOwner,
    )

fun HouseholdEntity.toDto(members: List<HouseholdMemberDto>) =
    HouseholdDto(
        id = id,
        name = name,
        members = members,
        inviteLink = inviteLink,
        isOwner = isOwner,
    )

fun HouseholdWithMembers.toDto() = household.toDto(members.map { it.toDto() })

fun HouseholdWithMembers.toDomain(): Household {
  val memberList = members.map { it.toDomain() }
  return Household(
      id = household.id,
      name = household.name,
      members = memberList,
      isOwner = household.isOwner,
      inviteLink = household.inviteLink,
      balance = memberList.find { it.isCurrentUser }?.balance ?: 0.0,
  )
}

fun HouseholdDto.toDomain(): Household {
  val memberList = members.map { it.toDomain() }
  return Household(
      id = id,
      name = name,
      members = memberList,
      isOwner = isOwner,
      inviteLink = inviteLink,
      balance = memberList.find { it.isCurrentUser }?.balance ?: 0.0,
  )
}

fun HouseholdMemberDto.toEntity(householdId: String) =
    MemberEntity(
        householdId = householdId,
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
        balance = balance,
    )

fun MemberEntity.toDto() =
    HouseholdMemberDto(
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

fun HouseholdMemberDto.toDomain() =
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
        householdId = householdId,
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
        householdId = householdId,
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
        householdId = householdId,
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
        householdId = householdId,
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
