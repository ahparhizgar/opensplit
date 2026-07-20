package com.opensplit.features.expense

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.features.household.HouseholdRepository
import java.util.*
import kotlin.time.Instant

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

    val participants =
        request.participants.map {
          ExpenseParticipantRecord(
              userId = it.userId,
              paidAmount = it.paidShare,
              owedAmount = it.owedShare,
          )
        }

    val expense =
        ExpenseRecord(
            id = UUID.randomUUID().toString(),
            householdId = householdId,
            title = request.title,
            amount = request.amount,
            payerId = payerId,
            createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            participants = participants,
        )
    expenseRepository.createExpense(expense)
    return expense.toDto()
  }

  fun getExpenses(householdId: String): List<ExpenseDto> {
    return expenseRepository.findExpensesByHouseholdId(householdId).map { it.toDto() }
  }
}
