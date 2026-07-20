package com.opensplit.features.expense

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
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
    val splitMethod: SplitMethod,
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
        splitMethod = splitMethod,
    )

fun ExpenseParticipantRecord.toDto() =
    ParticipantShareDto(
        userId = userId,
        paidShare = paidAmount,
        owedShare = owedAmount,
        netBalance = paidAmount - owedAmount,
    )

class NotAMemberException : RuntimeException()
