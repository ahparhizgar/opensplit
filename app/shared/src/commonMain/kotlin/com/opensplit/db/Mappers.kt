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
        balance = balance,
        isOwner = isOwner,
    )

fun HouseholdEntity.toDto(members: List<HouseholdMemberDto>) =
    HouseholdDto(
        id = id,
        name = name,
        members = members,
        inviteLink = inviteLink,
        balance = balance,
        isOwner = isOwner,
    )

fun HouseholdWithMembers.toDto() = household.toDto(members.map { it.toDto() })

fun HouseholdWithMembers.toDomain() =
    Household(
        id = household.id,
        name = household.name,
        members = members.map { it.toDomain() },
        isOwner = household.isOwner,
        inviteLink = household.inviteLink,
        balance = household.balance,
    )

fun HouseholdDto.toDomain() =
    Household(
        id = id,
        name = name,
        members = members.map { it.toDomain() },
        isOwner = isOwner,
        inviteLink = inviteLink,
        balance = balance,
    )

fun HouseholdMemberDto.toEntity(householdId: String) =
    MemberEntity(
        householdId = householdId,
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
        balance = balance,
        balanceCurrency = balanceCurrency,
    )

fun MemberEntity.toDto() =
    HouseholdMemberDto(
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
        balance = balance,
        balanceCurrency = balanceCurrency,
    )

fun MemberEntity.toDomain() =
    Member(
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
        balance = balance,
        balanceCurrency = balanceCurrency,
    )

fun HouseholdMemberDto.toDomain() =
    Member(
        userId = userId,
        name = name,
        email = email,
        isOwner = isOwner,
        isCurrentUser = isCurrentUser,
        balance = balance,
        balanceCurrency = balanceCurrency,
    )

fun ExpenseDto.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED) =
    ExpenseEntity(
        id = id,
        householdId = householdId,
        title = title,
        amount = amount,
        payerId = payerId,
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
        payerId = payerId,
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
        payerId = payerId,
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
        payerId = payerId,
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
