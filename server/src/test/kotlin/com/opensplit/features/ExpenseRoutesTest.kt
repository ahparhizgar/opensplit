package com.opensplit.features

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpenseRoutesTest {
  @Test
  fun createExpense_success() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    val response =
        client.post("/households/${household.id}/expenses") {
          setBody(CreateExpenseRequest(title = "Pizza", amount = 25.0))
        }

    assertEquals(HttpStatusCode.Created, response.status)
    val expense = response.body<ExpenseDto>()
    assertEquals("Pizza", expense.title)
    assertEquals(25.0, expense.amount)
    assertEquals(household.id, expense.householdId)
  }

  @Test
  fun createExpense_invalidData() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    val response =
        client.post("/households/${household.id}/expenses") {
          setBody(CreateExpenseRequest(title = "", amount = -5.0))
        }

    assertEquals(HttpStatusCode.BadRequest, response.status)
    val error = response.body<ErrorResponse>()
    assertTrue(error.errors.containsKey("title"))
    assertTrue(error.errors.containsKey("amount"))
  }
}
