package com.opensplit.features.expense

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.features.auth.UserPrincipal
import com.opensplit.features.household.HouseholdRepository
import java.util.*
import kotlin.time.Instant

class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val householdRepository: HouseholdRepository,
) {
  fun createExpense(
      householdId: String,
      request: CreateExpenseRequest,
  ): ExpenseDto {
    if (!householdRepository.hasMembership(householdId, request.payerId)) {
      throw NotAMemberException()
    }

    val participants =
        request.participants.map {
          ExpenseParticipantRecord(
              userId = it.userId,
              paidAmount = it.paidShare,
              owedAmount = it.consumedShare,
          )
        }

    val expense =
        ExpenseRecord(
            id = UUID.randomUUID().toString(),
            householdId = householdId,
            title = request.title,
            amount = request.amount,
            payerId = request.payerId,
            createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            participants = participants,
            splitMethod = request.splitMethod,
        )
    expenseRepository.createExpense(expense)
    return expense.toDto()
  }

  fun getExpenses(householdId: String): List<ExpenseDto> {
    return expenseRepository.findExpensesByHouseholdId(householdId).map { it.toDto() }
  }

  fun deleteExpense(user: UserPrincipal, householdId: String, expenseId: String) {
    if (!householdRepository.hasMembership(householdId, user.userId)) {
      throw NotAMemberException()
    }
    expenseRepository.deleteExpense(expenseId)
  }

  fun updateExpense(
      user: UserPrincipal,
      householdId: String,
      expenseId: String,
      request: CreateExpenseRequest,
  ): ExpenseDto {
    if (!householdRepository.hasMembership(householdId, user.userId)) {
      throw NotAMemberException()
    }

    val existingExpense =
        expenseRepository.findExpenseById(expenseId) ?: throw ExpenseNotFoundException()

    // Verify the expense belongs to the specified household
    if (existingExpense.householdId != householdId) {
      throw ExpenseNotFoundException()
    }

    val participants =
        request.participants.map {
          ExpenseParticipantRecord(
              userId = it.userId,
              paidAmount = it.paidShare,
              owedAmount = it.consumedShare,
          )
        }

    val updatedExpense =
        existingExpense.copy(
            title = request.title,
            amount = request.amount,
            payerId = request.payerId,
            participants = participants,
            splitMethod = request.splitMethod,
        )

    expenseRepository.updateExpense(updatedExpense)
    return updatedExpense.toDto()
  }
}

class ExpenseNotFoundException : Exception()
