package com.opensplit.features.expense

import com.opensplit.dto.expense.ExpenseDto
import kotlin.time.Instant

data class ExpenseRecord(
    val id: String,
    val householdId: String,
    val title: String,
    val amount: Double,
    val payerId: String,
    val createdAt: Instant,
)

fun ExpenseRecord.toDto() =
    ExpenseDto(
        id = id,
        householdId = householdId,
        title = title,
        amount = amount,
        payerId = payerId,
        createdAt = createdAt,
    )
