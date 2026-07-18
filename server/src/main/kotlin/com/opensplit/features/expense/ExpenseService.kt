package com.opensplit.features.expense

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.features.household.HouseholdRepository
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val householdRepository: HouseholdRepository,
) {
  fun createExpense(
      householdId: String,
      payerId: String,
      request: CreateExpenseRequest,
  ): ExpenseDto {
    if (!householdRepository.hasMembership(householdId, payerId)) {
      throw NotAMemberException()
    }
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
