package com.opensplit.features.expense

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.features.auth.UserPrincipal
import com.opensplit.features.group.GroupRepository
import java.util.*
import kotlin.time.Clock

class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
) {
  fun createExpense(
      groupId: String,
      request: CreateExpenseRequest,
      creator: String,
  ): ExpenseDto {
    if (!groupRepository.hasMembership(groupId, creator)) {
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
            groupId = groupId,
            title = request.title,
            amount = request.amount,
            creator = creator,
            createdAt = Clock.System.now(),
            participants = participants,
            splitMethod = request.splitMethod,
        )
    expenseRepository.createExpense(expense)
    return expense.toDto()
  }

  fun getExpenses(groupId: String): List<ExpenseDto> {
    return expenseRepository.findExpensesByGroupId(groupId).map { it.toDto() }
  }

  fun deleteExpense(user: UserPrincipal, groupId: String, expenseId: String) {
    if (!groupRepository.hasMembership(groupId, user.userId)) {
      throw NotAMemberException()
    }
    expenseRepository.deleteExpense(expenseId)
  }

  fun updateExpense(
      user: UserPrincipal,
      groupId: String,
      expenseId: String,
      request: CreateExpenseRequest,
  ): ExpenseDto {
    if (!groupRepository.hasMembership(groupId, user.userId)) {
      throw NotAMemberException()
    }

    val existingExpense =
        expenseRepository.findExpenseById(expenseId) ?: throw ExpenseNotFoundException()

    // Verify the expense belongs to the specified group
    if (existingExpense.groupId != groupId) {
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
            participants = participants,
            splitMethod = request.splitMethod,
        )

    expenseRepository.updateExpense(updatedExpense)
    return updatedExpense.toDto()
  }
}

class ExpenseNotFoundException : Exception()
