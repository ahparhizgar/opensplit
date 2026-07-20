package com.opensplit.features

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
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
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 25.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 25.0,
                              owedShare = 25.0,
                              netBalance = 0.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(household.members[0].userId)),
              )
          )
        }

    assertEquals(HttpStatusCode.Created, response.status)
    val expense = response.body<ExpenseDto>()
    assertEquals("Pizza", expense.title)
    assertEquals(25.0, expense.amount)
    assertEquals(household.id, expense.householdId)
    assertEquals(1, expense.participants.size)
    assertEquals(25.0, expense.participants[0].paidShare)
  }

  @Test
  fun createExpense_complexSplit() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    val response =
        client.post("/households/${household.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 100.0,
                              owedShare = 60.0,
                              netBalance = 40.0,
                          ),
                          ParticipantShareDto(
                              userId = "other-user",
                              paidShare = 0.0,
                              owedShare = 40.0,
                              netBalance = -40.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Exact(
                          mapOf(household.members[0].userId to 60.0, "other-user" to 40.0)
                      ),
              )
          )
        }

    assertEquals(HttpStatusCode.Created, response.status)
    val expense = response.body<ExpenseDto>()
    assertEquals(2, expense.participants.size)
    val p1 = expense.participants.find { it.userId == household.members[0].userId }!!
    assertEquals(100.0, p1.paidShare)
    assertEquals(60.0, p1.owedShare)
  }

  @Test
  fun createExpense_invalidData() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    val response =
        client.post("/households/${household.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "",
                  amount = -5.0,
                  participants = emptyList(),
                  splitMethod = SplitMethod.Equally(emptyList()),
              )
          )
        }

    assertEquals(HttpStatusCode.BadRequest, response.status)
    val error = response.body<ErrorResponse>()
    assertTrue(error.errors.containsKey("title"))
    assertTrue(error.errors.containsKey("amount"))
  }
}
