package com.opensplit.dto.expense

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    val id: String,
    val householdId: String,
    val title: String,
    val amount: Double,
    val payerId: String,
    val createdAt: Instant,
)

@Serializable data class CreateExpenseRequest(val title: String, val amount: Double)
