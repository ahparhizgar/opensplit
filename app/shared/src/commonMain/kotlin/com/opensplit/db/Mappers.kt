package com.opensplit.db

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
        participants = participants,
        splitMethod = Json.decodeFromString(splitMethodJson),
        syncStatus = syncStatus,
    )

fun ParticipantShareDto.toEntity(expenseId: String) =
    ParticipantEntity(
        expenseId = expenseId,
        userId = userId,
        paidShare = paidShare,
        owedShare = owedShare,
        netBalance = netBalance,
    )

fun ParticipantEntity.toDto() =
    ParticipantShareDto(
        userId = userId,
        paidShare = paidShare,
        owedShare = owedShare,
        netBalance = netBalance,
    )
