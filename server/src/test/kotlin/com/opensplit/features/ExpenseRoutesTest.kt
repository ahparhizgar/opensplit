package com.opensplit.features

import com.opensplit.createAuthenticatedClient
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
import io.ktor.client.request.put
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
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 25.0,
                              consumedShare = 25.0,
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
    assertEquals(1, expense.shares.size)
    assertEquals(25.0, expense.shares[0].paidShare)
  }

  @Test
  fun createExpense_complexSplit() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    // Create another user
    val signUpResult =
        client
            .post("/users") {
              setBody(
                  com.opensplit.dto.auth.SignUpRequest("other@example.com", "password123", "Other")
              )
            }
            .body<com.opensplit.dto.auth.AuthResult>()

    val otherUserId = signUpResult.userId
    val otherUserClient = createAuthenticatedClient(signUpResult.accessToken)

    // Join the household with the other user
    otherUserClient.post("/households/join") {
      setBody(com.opensplit.dto.household.JoinHouseholdRequest(household.inviteLink))
    }

    val response =
        client.post("/households/${household.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries",
                  amount = 100.0,
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 60.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 40.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Unequally(
                          mapOf(household.members[0].userId to 60.0, otherUserId to 40.0)
                      ),
              )
          )
        }

    assertEquals(HttpStatusCode.Created, response.status)
    val expense = response.body<ExpenseDto>()
    assertEquals(2, expense.shares.size)
    val p1 = expense.shares.find { it.userId == household.members[0].userId }!!
    assertEquals(100.0, p1.paidShare)
    assertEquals(60.0, p1.consumedShare)
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
                  payerId = "",
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

  @Test
  fun updateExpense_successfullyUpdateTitleAndAmount() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    // Create another user
    val signUpResult =
        client
            .post("/users") {
              setBody(
                  com.opensplit.dto.auth.SignUpRequest("other@example.com", "password123", "Other")
              )
            }
            .body<com.opensplit.dto.auth.AuthResult>()

    val otherUserId = signUpResult.userId
    val otherUserClient = createAuthenticatedClient(signUpResult.accessToken)

    // Join the household with the other user
    otherUserClient.post("/households/join") {
      setBody(com.opensplit.dto.household.JoinHouseholdRequest(household.inviteLink))
    }

    // Create initial expense
    val createResponse =
        client.post("/households/${household.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Old Title",
                  amount = 100.0,
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 50.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 50.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Equally(listOf(household.members[0].userId, otherUserId)),
              )
          )
        }
    val createdExpense = createResponse.body<ExpenseDto>()

    // Update the expense
    val updateResponse =
        client.put("/households/${household.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "New Title",
                  amount = 150.0,
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 150.0,
                              consumedShare = 75.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 75.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Equally(listOf(household.members[0].userId, otherUserId)),
              )
          )
        }

    assertEquals(HttpStatusCode.OK, updateResponse.status)
    val updatedExpense = updateResponse.body<ExpenseDto>()
    assertEquals("New Title", updatedExpense.title)
    assertEquals(150.0, updatedExpense.amount)
    assertEquals(2, updatedExpense.shares.size)
    assertEquals(75.0, updatedExpense.shares[0].consumedShare)
  }

  @Test
  fun updateExpense_changePayer() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    // Create another user
    val signUpResult =
        client
            .post("/users") {
              setBody(
                  com.opensplit.dto.auth.SignUpRequest("other@example.com", "password123", "Other")
              )
            }
            .body<com.opensplit.dto.auth.AuthResult>()

    val otherUserId = signUpResult.userId
    val otherUserClient = createAuthenticatedClient(signUpResult.accessToken)

    // Join the household
    otherUserClient.post("/households/join") {
      setBody(com.opensplit.dto.household.JoinHouseholdRequest(household.inviteLink))
    }

    // Create initial expense with User A as payer
    val createResponse =
        client.post("/households/${household.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 100.0,
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 50.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 50.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Equally(listOf(household.members[0].userId, otherUserId)),
              )
          )
        }
    val createdExpense = createResponse.body<ExpenseDto>()

    // Update expense to change payer to User B
    val updateResponse =
        client.put("/households/${household.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 100.0,
                  payerId = otherUserId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 0.0,
                              consumedShare = 50.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 100.0,
                              consumedShare = 50.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Equally(listOf(household.members[0].userId, otherUserId)),
              )
          )
        }

    assertEquals(HttpStatusCode.OK, updateResponse.status)
    val updatedExpense = updateResponse.body<ExpenseDto>()
    assertEquals(otherUserId, updatedExpense.payerId)
    val otherUserShare = updatedExpense.shares.find { it.userId == otherUserId }!!
    assertEquals(100.0, otherUserShare.paidShare)
  }

  @Test
  fun updateExpense_changeSplitMethodFromEqualToUnequal() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    // Create another user
    val signUpResult =
        client
            .post("/users") {
              setBody(
                  com.opensplit.dto.auth.SignUpRequest("other@example.com", "password123", "Other")
              )
            }
            .body<com.opensplit.dto.auth.AuthResult>()

    val otherUserId = signUpResult.userId
    val otherUserClient = createAuthenticatedClient(signUpResult.accessToken)

    // Join the household
    otherUserClient.post("/households/join") {
      setBody(com.opensplit.dto.household.JoinHouseholdRequest(household.inviteLink))
    }

    // Create initial expense with Equal split
    val createResponse =
        client.post("/households/${household.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries",
                  amount = 100.0,
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 50.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 50.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Equally(listOf(household.members[0].userId, otherUserId)),
              )
          )
        }
    val createdExpense = createResponse.body<ExpenseDto>()

    // Update to Unequal split
    val updateResponse =
        client.put("/households/${household.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries",
                  amount = 100.0,
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 60.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 40.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Unequally(
                          mapOf(household.members[0].userId to 60.0, otherUserId to 40.0)
                      ),
              )
          )
        }

    assertEquals(HttpStatusCode.OK, updateResponse.status)
    val updatedExpense = updateResponse.body<ExpenseDto>()
    assertTrue(updatedExpense.splitMethod is SplitMethod.Unequally)
    val p1 = updatedExpense.shares.find { it.userId == household.members[0].userId }!!
    assertEquals(60.0, p1.consumedShare)
  }

  @Test
  fun updateExpense_nonExistentExpense() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    val response =
        client.put("/households/${household.id}/expenses/non-existent-id") {
          setBody(
              CreateExpenseRequest(
                  title = "Test",
                  amount = 100.0,
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 100.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(household.members[0].userId)),
              )
          )
        }

    assertEquals(HttpStatusCode.NotFound, response.status)
  }

  @Test
  fun updateExpense_notAMember() = testOpenSplit {
    val household =
        client.post("/households") { setBody(CreateHouseholdRequest("Home")) }.body<HouseholdDto>()

    // Create expense
    val createResponse =
        client.post("/households/${household.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 100.0,
                  payerId = household.members[0].userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = household.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 100.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(household.members[0].userId)),
              )
          )
        }
    val createdExpense = createResponse.body<ExpenseDto>()

    // Create another user who is NOT in the household
    val signUpResult =
        client
            .post("/users") {
              setBody(
                  com.opensplit.dto.auth.SignUpRequest(
                      "outsider@example.com",
                      "password123",
                      "Outsider",
                  )
              )
            }
            .body<com.opensplit.dto.auth.AuthResult>()

    val outsiderClient = createAuthenticatedClient(signUpResult.accessToken)

    // Try to update the expense
    val response =
        outsiderClient.put("/households/${household.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "Hacked",
                  amount = 999.0,
                  payerId = signUpResult.userId,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = signUpResult.userId,
                              paidShare = 999.0,
                              consumedShare = 999.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(signUpResult.userId)),
              )
          )
        }

    assertEquals(HttpStatusCode.Forbidden, response.status)
  }
}
