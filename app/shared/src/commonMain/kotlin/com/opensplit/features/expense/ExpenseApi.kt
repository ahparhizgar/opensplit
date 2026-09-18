package com.opensplit.features.expense

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface ExpenseApi {
  suspend fun createExpense(
      groupId: String,
      title: String,
      amount: Double,
      creator: String,
      participants: List<ParticipantShareDto>,
      splitMethod: SplitMethod,
  ): ExpenseDto

  suspend fun deleteExpense(groupId: String, expenseId: String)
}

class KtorExpenseApi(private val client: HttpClient) : ExpenseApi {

  override suspend fun createExpense(
      groupId: String,
      title: String,
      amount: Double,
      creator: String,
      participants: List<ParticipantShareDto>,
      splitMethod: SplitMethod,
  ): ExpenseDto {
    val response =
        client.post("groups/$groupId/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = title,
                  amount = amount,
                  participants = participants,
                  splitMethod = splitMethod,
              )
          )
        }
    return response.body<ExpenseDto>()
  }

  override suspend fun deleteExpense(groupId: String, expenseId: String) {
    client.delete("groups/$groupId/expenses/$expenseId")
  }
}
