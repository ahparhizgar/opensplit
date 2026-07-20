package com.opensplit.features.expense

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface ExpenseApi {
  suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
      participants: List<ParticipantShareDto>,
  ): ExpenseDto

  suspend fun getExpenses(householdId: String): List<ExpenseDto>
}

class KtorExpenseApi(private val client: HttpClient) : ExpenseApi {

  override suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
      participants: List<ParticipantShareDto>,
  ): ExpenseDto {
    val response =
        client.post("households/$householdId/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = title,
                  amount = amount,
                  participants = participants,
              )
          )
        }
    return response.body<ExpenseDto>()
  }

  override suspend fun getExpenses(householdId: String): List<ExpenseDto> {
    val response = client.get("households/$householdId/expenses")
    return response.body<List<ExpenseDto>>()
  }
}
