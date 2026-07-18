package com.opensplit.features.expense

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExpenseService(private val expenseRepository: ExpenseRepository) {
  fun createExpense(
      householdId: String,
      payerId: String,
      request: CreateExpenseRequest,
  ): ExpenseDto {
    val expense =
        ExpenseRecord(
            id = Uuid.random().toString(),
            householdId = householdId,
            title = request.title,
            amount = request.amount,
            payerId = payerId,
            createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
        )
    expenseRepository.createExpense(expense)
    return expense.toDto()
  }

  fun getExpenses(householdId: String): List<ExpenseDto> {
    return expenseRepository.findExpensesByHouseholdId(householdId).map { it.toDto() }
  }
}
