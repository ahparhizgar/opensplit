package com.opensplit.features.expense

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.features.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

interface ExpenseApi {
  suspend fun createExpense(householdId: String, title: String, amount: Double): ExpenseDto
}

class KtorExpenseApi(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : ExpenseApi {

  private suspend fun handleUnauthorized() {
    tokenStorage.clearAccessToken()
  }

  override suspend fun createExpense(
      householdId: String,
      title: String,
      amount: Double,
  ): ExpenseDto {
    val response =
        client.post("households/$householdId/expenses") {
          setBody(CreateExpenseRequest(title = title, amount = amount))
        }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<ExpenseDto>()
  }
}
