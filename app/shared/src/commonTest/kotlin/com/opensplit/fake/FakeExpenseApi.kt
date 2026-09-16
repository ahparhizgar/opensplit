package com.opensplit.fake

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.util.FakeService
import kotlin.time.Instant

class FakeExpenseApi : ExpenseApi, FakeService {
  override var errorToThrow: Exception? = null

  val createdExpenses = mutableListOf<ExpenseDto>()
  val deletedCalls = mutableListOf<Pair<String, String>>()

  override suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
      creator: String,
      participants: List<ParticipantShareDto>,
      splitMethod: SplitMethod,
  ): ExpenseDto = fakeApiCall {
    val dto =
        ExpenseDto(
            id = "expense-${createdExpenses.size + 1}",
            householdId = householdId,
            title = title,
            amount = amount,
            creator = creator,
            createdAt = Instant.fromEpochMilliseconds(123456789L),
            shares = participants,
            splitMethod = splitMethod,
        )
    createdExpenses.add(dto)
    dto
  }

  override suspend fun deleteExpense(householdId: String, expenseId: String) = fakeApiCall {
    deletedCalls.add(householdId to expenseId)
    Unit
  }
}
