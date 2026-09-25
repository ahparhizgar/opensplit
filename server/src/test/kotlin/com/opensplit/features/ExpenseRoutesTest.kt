package com.opensplit.features

import com.opensplit.createClient
import com.opensplit.createExpenseRequest
import com.opensplit.createExpenseWith1MemberFixture
import com.opensplit.createExpenseWith2MembersFixture
import com.opensplit.createGroup
import com.opensplit.createGroupWith2MembersFixture
import com.opensplit.deleteExpense
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.FakeCreateExpenseRequestFactory
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.getGroup
import com.opensplit.testOpenSplit
import com.opensplit.updateExpenseRequest
import io.ktor.client.call.body
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
        client.createExpenseRequest(
            groupId = group.id,
            request =
                FakeCreateExpenseRequestFactory.createEqual(
                    userIds = listOf(group.members[0].userId),
                    title = "Pizza",
                    amount = 25.0,
                ),
        )

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
    val f = createGroupWith2MembersFixture()

    val response =
        f.client1.createExpenseRequest(
            groupId = f.group.id,
            request =
                FakeCreateExpenseRequestFactory.createUnequal(
                    consumedShares = mapOf(f.user1.userId to 60.0, f.user2.userId to 40.0),
                    payerId = f.user1.userId,
                    title = "Groceries",
                    amount = 100.0,
                ),
        )

    assertEquals(HttpStatusCode.Created, response.status)
    val expense = response.body<ExpenseDto>()
    assertEquals(2, expense.shares.size)
    val p1 = expense.shares.find { it.userId == f.user1.userId }!!
    assertEquals(100.0, p1.paidShare)
    assertEquals(60.0, p1.consumedShare)
  }

  @Test
  fun createExpense_invalidData() = testOpenSplit {
    val group = client.createGroup()

    val response =
        client.createExpenseRequest(
            groupId = group.id,
            request =
                FakeCreateExpenseRequestFactory.create(
                    title = "",
                    amount = -5.0,
                    participants = emptyList(),
                    splitMethod = SplitMethod.Equally(emptyList()),
                ),
        )

    assertEquals(HttpStatusCode.BadRequest, response.status)
    val error = response.body<ErrorResponse>()
    assertTrue(error.errors.containsKey("title"))
    assertTrue(error.errors.containsKey("amount"))
  }

  @Test
  fun updateExpense_successfullyUpdateTitleAndAmount() = testOpenSplit {
    val f = createExpenseWith2MembersFixture(title = "Old Title")

    val updateResponse =
        f.client1.updateExpenseRequest(
            groupId = f.group.id,
            expenseId = f.expense.id,
            request =
                FakeCreateExpenseRequestFactory.createEqual(
                    userIds = listOf(f.user1.userId, f.user2.userId),
                    payerId = f.user1.userId,
                    title = "New Title",
                    amount = 150.0,
                ),
        )

    assertEquals(HttpStatusCode.OK, updateResponse.status)
    val updatedExpense = updateResponse.body<ExpenseDto>()
    assertEquals("New Title", updatedExpense.title)
    assertEquals(150.0, updatedExpense.amount)
    assertEquals(2, updatedExpense.shares.size)
    assertEquals(75.0, updatedExpense.shares[0].consumedShare)
  }

  @Test
  fun updateExpense_changePayer() = testOpenSplit {
    val f = createExpenseWith2MembersFixture(title = "Pizza")

    val updateResponse =
        f.client1.updateExpenseRequest(
            groupId = f.group.id,
            expenseId = f.expense.id,
            request =
                FakeCreateExpenseRequestFactory.createEqual(
                    userIds = listOf(f.user1.userId, f.user2.userId),
                    payerId = f.user1.userId,
                    title = "Pizza",
                    amount = 100.0,
                ),
        )

    assertEquals(HttpStatusCode.OK, updateResponse.status)
    val updatedExpense = updateResponse.body<ExpenseDto>()
    assertEquals(f.user1.userId, updatedExpense.creator)
    val otherUserShare = updatedExpense.shares.find { it.userId == f.user2.userId }!!
    assertEquals(0.0, otherUserShare.paidShare)
  }

  @Test
  fun updateExpense_changeSplitMethodFromEqualToUnequal() = testOpenSplit {
    val f = createExpenseWith2MembersFixture()

    val updateResponse =
        f.client1.updateExpenseRequest(
            groupId = f.group.id,
            expenseId = f.expense.id,
            request =
                FakeCreateExpenseRequestFactory.createUnequal(
                    consumedShares = mapOf(f.user1.userId to 60.0, f.user2.userId to 40.0),
                    payerId = f.user1.userId,
                    title = "Groceries",
                    amount = 100.0,
                ),
        )

    assertEquals(HttpStatusCode.OK, updateResponse.status)
    val updatedExpense = updateResponse.body<ExpenseDto>()
    assertTrue(updatedExpense.splitMethod is SplitMethod.Unequally)
    val p1 = updatedExpense.shares.find { it.userId == f.user1.userId }!!
    assertEquals(60.0, p1.consumedShare)
  }

  @Test
  fun updateExpense_nonExistentExpense() = testOpenSplit {
    val group = client.createGroup()

    val response =
        client.updateExpenseRequest(
            groupId = group.id,
            expenseId = "non-existent-id",
            request =
                FakeCreateExpenseRequestFactory.createEqual(
                    userIds = listOf(group.members[0].userId),
                ),
        )

    assertEquals(HttpStatusCode.NotFound, response.status)
  }

  @Test
  fun updateExpense_notAMember() = testOpenSplit {
    val f = createExpenseWith1MemberFixture(title = "Pizza", amount = 100.0)

    val outsiderClient = createClient("Outsider")

    val response =
        outsiderClient.updateExpenseRequest(
            groupId = f.group.id,
            expenseId = f.expense.id,
            request =
                FakeCreateExpenseRequestFactory.createEqual(
                    userIds = listOf("outsider-id"),
                ),
        )

    assertEquals(HttpStatusCode.Forbidden, response.status)
  }

  @Test
  fun expenseMutations_updateGroupLastInteractionAt() = testOpenSplit {
    val group = client.createGroup("Activity Group")
    val initialInteraction = group.lastInteractionAt

    // Wait a tiny bit or let time progress to ensure distinct timestamp
    delay(10.milliseconds)

    // 1. Create Expense updates lastInteractionAt
    val createRequest =
        FakeCreateExpenseRequestFactory.createEqual(
            userIds = listOf(group.members[0].userId),
        )
    val expense = client.createExpenseRequest(group.id, createRequest).body<ExpenseDto>()

    val groupAfterCreate = client.getGroup(group.id)
    assertTrue(groupAfterCreate.lastInteractionAt >= initialInteraction)

    delay(10.milliseconds)

    // 2. Update Expense updates lastInteractionAt
    val updateRequest =
        FakeCreateExpenseRequestFactory.createEqual(
            userIds = listOf(group.members[0].userId),
        )
    client.updateExpenseRequest(group.id, expense.id, updateRequest)

    val groupAfterUpdate = client.getGroup(group.id)
    assertTrue(groupAfterUpdate.lastInteractionAt >= groupAfterCreate.lastInteractionAt)

    delay(10.milliseconds)

    // 3. Delete Expense updates lastInteractionAt
    val deleteResponse = client.deleteExpense(group.id, expense.id)
    assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

    val groupAfterDelete = client.getGroup(group.id)
    assertTrue(groupAfterDelete.lastInteractionAt >= groupAfterUpdate.lastInteractionAt)
  }
}
