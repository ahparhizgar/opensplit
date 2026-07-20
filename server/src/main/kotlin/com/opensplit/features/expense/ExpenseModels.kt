package com.opensplit.features.expense

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import kotlin.time.Instant

data class ExpenseParticipantRecord(
    val userId: String,
    val paidAmount: Double,
    val owedAmount: Double,
)

data class ExpenseRecord(
    val id: String,
    val householdId: String,
    val title: String,
    val amount: Double,
    val payerId: String,
    val createdAt: Instant,
    val participants: List<ExpenseParticipantRecord> = emptyList(),
)

fun ExpenseRecord.toDto() =
    ExpenseDto(
        id = id,
        householdId = householdId,
        title = title,
        amount = amount,
        payerId = payerId,
        createdAt = createdAt,
        participants = participants.map { it.toDto() },
    )

fun ExpenseParticipantRecord.toDto() =
    ParticipantShareDto(
        userId = userId,
        paidShare = paidAmount,
        owedShare = owedAmount,
        netBalance = paidAmount - owedAmount,
    )

class NotAMemberException : RuntimeException()
