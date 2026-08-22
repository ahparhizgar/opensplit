package com.opensplit.fake

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.util.FakeService
import kotlin.time.Instant

class FakeExpenseApi : ExpenseApi, FakeService {
  override var errorToThrow: Exception? = null

  override suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
      creator: String,
      participants: List<ParticipantShareDto>,
      splitMethod: SplitMethod,
  ): ExpenseDto = fakeApiCall {
    ExpenseDto(
        id = "expense-1",
        householdId = householdId,
        title = title,
        amount = amount,
        creator = creator,
        createdAt = Instant.fromEpochMilliseconds(123456789L),
        shares = participants,
        splitMethod = splitMethod,
    )
  }

  override suspend fun deleteExpense(householdId: String, expenseId: String) = fakeApiCall {
    // No-op
  }
}
