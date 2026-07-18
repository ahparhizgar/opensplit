package com.opensplit.fake

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.util.FakeService
import kotlin.time.Instant

class FakeExpenseApi : ExpenseApi, FakeService {
  override var errorToThrow: Exception? = null

  override suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
  ): ExpenseDto = fakeApiCall {
    ExpenseDto(
        id = "expense-1",
        householdId = householdId,
        title = title,
        amount = amount,
        payerId = "user-1",
        createdAt = Instant.fromEpochMilliseconds(123456789L),
    )
  }
}
