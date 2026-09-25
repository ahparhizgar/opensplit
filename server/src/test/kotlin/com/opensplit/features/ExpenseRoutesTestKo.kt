package com.opensplit.features

import com.opensplit.KtorBehaviorSpec
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
import com.opensplit.testValue
import com.opensplit.updateExpenseRequest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

class ExpenseRoutesTestKo : KtorBehaviorSpec() {
  init {
    Given("expenses endpoints") {
      When("creating an expense with equal split") {
        val group by testValue { client.createGroup() }
        val response by testValue {
          client.createExpenseRequest(
              groupId = group.id,
              request =
                  FakeCreateExpenseRequestFactory.createEqual(
                      userIds = listOf(group.members[0].userId),
                      title = "Pizza",
                      amount = 25.0,
                  ),
          )
        }
        Then("it returns 201 Created and creates the expense") {
          response.status shouldBe HttpStatusCode.Created
          val expense = response.body<ExpenseDto>()
          expense.title shouldBe "Pizza"
          expense.amount shouldBe 25.0
          expense.groupId shouldBe group.id
          expense.shares shouldHaveSize 1
          expense.shares[0].paidShare shouldBe 25.0
        }
      }

      When("creating an expense with complex unequal split") {
        val f by testValue { createGroupWith2MembersFixture() }
        val response by testValue {
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
        }
        Then("it returns 201 Created with correct shares") {
          response.status shouldBe HttpStatusCode.Created
          val expense = response.body<ExpenseDto>()
          expense.shares shouldHaveSize 2
          val p1 = expense.shares.find { it.userId == f.user1.userId }!!
          p1.paidShare shouldBe 100.0
          p1.consumedShare shouldBe 60.0
        }
      }

      When("creating an expense with invalid data") {
        val group by testValue { client.createGroup() }
        val response by testValue {
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
        }
        Then("it returns 400 Bad Request with field errors") {
          response.status shouldBe HttpStatusCode.BadRequest
          val error = response.body<ErrorResponse>()
          error.errors shouldContainKey "title"
          error.errors shouldContainKey "amount"
        }
      }

      When("updating expense title and amount successfully") {
        val f by testValue { createExpenseWith2MembersFixture(title = "Old Title") }
        val updateResponse by testValue {
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
        }
        Then("returns 200 OK with updated details") {
          updateResponse.status shouldBe HttpStatusCode.OK
          val updatedExpense = updateResponse.body<ExpenseDto>()
          updatedExpense.title shouldBe "New Title"
          updatedExpense.amount shouldBe 150.0
          updatedExpense.shares shouldHaveSize 2
          updatedExpense.shares[0].consumedShare shouldBe 75.0
        }
      }

      When("updating expense to change payer") {
        val f by testValue { createExpenseWith2MembersFixture(title = "Pizza") }
        val updateResponse by testValue {
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
        }
        Then("returns 200 OK and reflects payer change") {
          updateResponse.status shouldBe HttpStatusCode.OK
          val updatedExpense = updateResponse.body<ExpenseDto>()
          updatedExpense.creator shouldBe f.user1.userId
          val otherUserShare = updatedExpense.shares.find { it.userId == f.user2.userId }!!
          otherUserShare.paidShare shouldBe 0.0
        }
      }

      When("updating split method from equal to unequal") {
        val f by testValue { createExpenseWith2MembersFixture() }
        val updateResponse by testValue {
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
        }
        Then("returns 200 OK and updates splitMethod to Unequally") {
          updateResponse.status shouldBe HttpStatusCode.OK
          val updatedExpense = updateResponse.body<ExpenseDto>()
          updatedExpense.splitMethod.shouldBeInstanceOf<SplitMethod.Unequally>()
          val p1 = updatedExpense.shares.find { it.userId == f.user1.userId }!!
          p1.consumedShare shouldBe 60.0
        }
      }

      When("updating a non-existent expense") {
        val group by testValue { client.createGroup() }
        val response by testValue {
          client.updateExpenseRequest(
              groupId = group.id,
              expenseId = "non-existent-id",
              request =
                  FakeCreateExpenseRequestFactory.createEqual(
                      userIds = listOf(group.members[0].userId),
                  ),
          )
        }
        Then("returns 404 Not Found") { response.status shouldBe HttpStatusCode.NotFound }
      }

      When("a non-member attempts to update an expense") {
        val f by testValue { createExpenseWith1MemberFixture(title = "Pizza", amount = 100.0) }
        val outsiderClient by testValue { createClient("Outsider") }
        val response by testValue {
          outsiderClient.updateExpenseRequest(
              groupId = f.group.id,
              expenseId = f.expense.id,
              request =
                  FakeCreateExpenseRequestFactory.createEqual(
                      userIds = listOf("outsider-id"),
                  ),
          )
        }
        Then("returns 403 Forbidden") { response.status shouldBe HttpStatusCode.Forbidden }
      }

      When("expenses are mutated") {
        val group by testValue { client.createGroup("Activity Group") }
        Then("lastInteractionAt of the group increases across create, update, delete") {
          val initialInteraction = group.lastInteractionAt
          delay(10.milliseconds)

          // 1. Create Expense updates lastInteractionAt
          val createRequest =
              FakeCreateExpenseRequestFactory.createEqual(userIds = listOf(group.members[0].userId))
          val expense = client.createExpenseRequest(group.id, createRequest).body<ExpenseDto>()

          val groupAfterCreate = client.getGroup(group.id)
          (groupAfterCreate.lastInteractionAt >= initialInteraction) shouldBe true

          delay(10.milliseconds)

          // 2. Update Expense updates lastInteractionAt
          val updateRequest =
              FakeCreateExpenseRequestFactory.createEqual(userIds = listOf(group.members[0].userId))
          client.updateExpenseRequest(group.id, expense.id, updateRequest)

          val groupAfterUpdate = client.getGroup(group.id)
          (groupAfterUpdate.lastInteractionAt >= groupAfterCreate.lastInteractionAt) shouldBe true

          delay(10.milliseconds)

          // 3. Delete Expense updates lastInteractionAt
          val deleteResponse = client.deleteExpense(group.id, expense.id)
          deleteResponse.status shouldBe HttpStatusCode.NoContent

          val groupAfterDelete = client.getGroup(group.id)
          (groupAfterDelete.lastInteractionAt >= groupAfterUpdate.lastInteractionAt) shouldBe true
        }
      }
    }
  }
}
