package com.opensplit.fake

import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.util.FakeService
import kotlin.time.Instant

class FakeExpenseApi : ExpenseApi, FakeService {
  override var errorToThrow: Exception? = null

  override suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
      participants: List<ParticipantShareDto>,
  ): ExpenseDto = fakeApiCall {
    ExpenseDto(
        id = "expense-1",
        householdId = householdId,
        title = title,
        amount = amount,
        payerId = participants.firstOrNull { it.paidShare > 0 }?.userId ?: "user-1",
        createdAt = Instant.fromEpochMilliseconds(123456789L),
        participants = participants,
    )
  }

  override suspend fun getExpenses(householdId: String): List<ExpenseDto> = fakeApiCall {
    emptyList()
  }
}
