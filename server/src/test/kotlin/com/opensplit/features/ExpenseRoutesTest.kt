package com.opensplit.features

import com.opensplit.createClientByToken
import com.opensplit.createClientWithResult
import com.opensplit.createGroup
import com.opensplit.dto.auth.AuthResult
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.group.CreateGroupRequest
import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.JoinGroupRequest
import com.opensplit.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

class ExpenseRoutesTest {
  @Test
  fun createExpense_success() = testOpenSplit {
    val group = client.createGroup()

    val response =
        client.post("/groups/${group.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 25.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 25.0,
                              consumedShare = 25.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId)),
              )
          )
        }

    assertEquals(HttpStatusCode.Created, response.status)
    val expense = response.body<ExpenseDto>()
    assertEquals("Pizza", expense.title)
    assertEquals(25.0, expense.amount)
    assertEquals(group.id, expense.groupId)
    assertEquals(1, expense.shares.size)
    assertEquals(25.0, expense.shares[0].paidShare)
  }

  @Test
  fun createExpense_complexSplit() = testOpenSplit {
    val group = client.createGroup()
    val (otherUserClient, userId) = createClientWithResult()
    // Join the group with the other user
    otherUserClient.post("/groups/join") { setBody(JoinGroupRequest(group.inviteLink)) }

    val response =
        client.post("/groups/${group.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 60.0,
                          ),
                          ParticipantShareDto(
                              userId = userId.userId,
                              paidShare = 0.0,
                              consumedShare = 40.0,
                          ),
                      ),
                  splitMethod =
                      SplitMethod.Unequally(
                          mapOf(group.members[0].userId to 60.0, userId.userId to 40.0)
                      ),
              )
          )
        }

    assertEquals(HttpStatusCode.Created, response.status)
    val expense = response.body<ExpenseDto>()
    assertEquals(2, expense.shares.size)
    val p1 = expense.shares.find { it.userId == group.members[0].userId }!!
    assertEquals(100.0, p1.paidShare)
    assertEquals(60.0, p1.consumedShare)
  }

  @Test
  fun createExpense_invalidData() = testOpenSplit {
    val group = client.createGroup()

    val response =
        client.post("/groups/${group.id}/expenses") {
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

  @Test
  fun updateExpense_successfullyUpdateTitleAndAmount() = testOpenSplit {
    val group = client.createGroup()

    // Create another user
    val signUpResult =
        client
            .post("/users") { setBody(SignUpRequest("other@example.com", "password123", "Other")) }
            .body<AuthResult>()

    val otherUserId = signUpResult.userId
    val otherUserClient = createClientByToken(signUpResult.accessToken)

    // Join the group with the other user
    otherUserClient.post("/groups/join") { setBody(JoinGroupRequest(group.inviteLink)) }

    // Create initial expense
    val createResponse =
        client.post("/groups/${group.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Old Title",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 50.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 50.0,
                          ),
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId, otherUserId)),
              )
          )
        }
    val createdExpense = createResponse.body<ExpenseDto>()

    // Update the expense
    val updateResponse =
        client.put("/groups/${group.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "New Title",
                  amount = 150.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 150.0,
                              consumedShare = 75.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 75.0,
                          ),
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId, otherUserId)),
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
    val group = client.createGroup()

    // Create another user
    val signUpResult =
        client
            .post("/users") { setBody(SignUpRequest("other@example.com", "password123", "Other")) }
            .body<AuthResult>()

    val otherUserId = signUpResult.userId
    val otherUserClient = createClientByToken(signUpResult.accessToken)

    // Join the group
    otherUserClient.post("/groups/join") { setBody(JoinGroupRequest(group.inviteLink)) }

    // Create initial expense with User A as payer
    val createResponse =
        client.post("/groups/${group.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 50.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 50.0,
                          ),
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId, otherUserId)),
              )
          )
        }
    val createdExpense = createResponse.body<ExpenseDto>()

    // Update expense to change payer to User B
    val updateResponse =
        client.put("/groups/${group.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 50.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 50.0,
                          ),
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId, otherUserId)),
              )
          )
        }

    assertEquals(HttpStatusCode.OK, updateResponse.status)
    val updatedExpense = updateResponse.body<ExpenseDto>()
    assertEquals(group.members[0].userId, updatedExpense.creator)
    val otherUserShare = updatedExpense.shares.find { it.userId == otherUserId }!!
    assertEquals(0.0, otherUserShare.paidShare)
  }

  @Test
  fun updateExpense_changeSplitMethodFromEqualToUnequal() = testOpenSplit {
    val group = client.createGroup()

    // Create another user
    val signUpResult =
        client
            .post("/users") { setBody(SignUpRequest("other@example.com", "password123", "Other")) }
            .body<AuthResult>()

    val otherUserId = signUpResult.userId
    val otherUserClient = createClientByToken(signUpResult.accessToken)

    // Join the group
    otherUserClient.post("/groups/join") { setBody(JoinGroupRequest(group.inviteLink)) }

    // Create initial expense with Equal split
    val createResponse =
        client.post("/groups/${group.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 50.0,
                          ),
                          ParticipantShareDto(
                              userId = otherUserId,
                              paidShare = 0.0,
                              consumedShare = 50.0,
                          ),
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId, otherUserId)),
              )
          )
        }
    val createdExpense = createResponse.body<ExpenseDto>()

    // Update to Unequal split
    val updateResponse =
        client.put("/groups/${group.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
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
                          mapOf(group.members[0].userId to 60.0, otherUserId to 40.0)
                      ),
              )
          )
        }

    assertEquals(HttpStatusCode.OK, updateResponse.status)
    val updatedExpense = updateResponse.body<ExpenseDto>()
    assertTrue(updatedExpense.splitMethod is SplitMethod.Unequally)
    val p1 = updatedExpense.shares.find { it.userId == group.members[0].userId }!!
    assertEquals(60.0, p1.consumedShare)
  }

  @Test
  fun updateExpense_nonExistentExpense() = testOpenSplit {
    val group = client.createGroup()

    val response =
        client.put("/groups/${group.id}/expenses/non-existent-id") {
          setBody(
              CreateExpenseRequest(
                  title = "Test",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 100.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId)),
              )
          )
        }

    assertEquals(HttpStatusCode.NotFound, response.status)
  }

  @Test
  fun updateExpense_notAMember() = testOpenSplit {
    val group = client.createGroup()

    // Create expense
    val createResponse =
        client.post("/groups/${group.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 100.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 100.0,
                              consumedShare = 100.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId)),
              )
          )
        }
    val createdExpense = createResponse.body<ExpenseDto>()

    // Create another user who is NOT in the group
    val signUpResult =
        client
            .post("/users") {
              setBody(
                  SignUpRequest(
                      "outsider@example.com",
                      "password123",
                      "Outsider",
                  )
              )
            }
            .body<AuthResult>()

    val outsiderClient = createClientByToken(signUpResult.accessToken)

    // Try to update the expense
    val response =
        outsiderClient.put("/groups/${group.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "Hacked",
                  amount = 999.0,
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

  @Test
  fun expenseMutations_updateGroupLastInteractionAt() = testOpenSplit {
    val group =
        client.post("/groups") { setBody(CreateGroupRequest("Activity Group")) }.body<GroupDto>()
    val initialInteraction = group.lastInteractionAt

    // Wait a tiny bit or let time progress to ensure distinct timestamp
    delay(10.milliseconds)

    // 1. Create Expense updates lastInteractionAt
    val createResponse =
        client.post("/groups/${group.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries",
                  amount = 50.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 50.0,
                              consumedShare = 50.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId)),
              )
          )
        }
    assertEquals(HttpStatusCode.Created, createResponse.status)
    val expense = createResponse.body<ExpenseDto>()

    val groupAfterCreate = client.get("/groups/${group.id}").body<GroupDto>()
    assertTrue(groupAfterCreate.lastInteractionAt >= initialInteraction)

    delay(10.milliseconds)

    // 2. Update Expense updates lastInteractionAt
    val updateResponse =
        client.put("/groups/${group.id}/expenses/${expense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "Groceries & Snacks",
                  amount = 60.0,
                  participants =
                      listOf(
                          ParticipantShareDto(
                              userId = group.members[0].userId,
                              paidShare = 60.0,
                              consumedShare = 60.0,
                          )
                      ),
                  splitMethod = SplitMethod.Equally(listOf(group.members[0].userId)),
              )
          )
        }
    assertEquals(HttpStatusCode.OK, updateResponse.status)

    val groupAfterUpdate = client.get("/groups/${group.id}").body<GroupDto>()
    assertTrue(groupAfterUpdate.lastInteractionAt >= groupAfterCreate.lastInteractionAt)

    delay(10.milliseconds)

    // 3. Delete Expense updates lastInteractionAt
    val deleteResponse = client.delete("/groups/${group.id}/expenses/${expense.id}")
    assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

    val groupAfterDelete = client.get("/groups/${group.id}").body<GroupDto>()
    assertTrue(groupAfterDelete.lastInteractionAt >= groupAfterUpdate.lastInteractionAt)
  }
}
