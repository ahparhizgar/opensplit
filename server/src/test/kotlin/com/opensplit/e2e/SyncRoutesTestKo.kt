package com.opensplit.e2e

import com.opensplit.dto.expense.FakeCreateExpenseRequestFactory
import com.opensplit.fixture.createExpense
import com.opensplit.fixture.createGroupWith1MemberFixture
import com.opensplit.fixture.createGroupWith2MembersFixture
import com.opensplit.fixture.deleteExpense
import com.opensplit.fixture.sync
import com.opensplit.fixture.updateExpense
import com.opensplit.util.KtorBehaviorSpec
import com.opensplit.util.createClient
import com.opensplit.util.testValue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode

class SyncRoutesTestKo : KtorBehaviorSpec() {
  init {
    Given("sync endpoints") {
      When("two users perform create, update, and delete flow on expenses") {
        val f by testValue { createGroupWith2MembersFixture() }
        Then("User B receives incremental updates and deletions across sync calls") {
          // Get initial sync version for User B
          val initialSync = f.client2.sync(sinceVersion = 0)
          val v0 = initialSync.latestVersion

          // User A creates an expense
          val createRequest =
              FakeCreateExpenseRequestFactory.createEqual(
                  userIds = listOf(f.user1.userId),
                  payerId = f.user1.userId,
                  title = "Pizza",
                  amount = 50.0,
              )
          val createdExpense = f.client1.createExpense(f.group.id, createRequest)

          // User B syncs changes since v0
          val syncAfterCreate = f.client2.sync(sinceVersion = v0)
          val v1 = syncAfterCreate.latestVersion
          (v1 > v0) shouldBe true
          syncAfterCreate.changedEntities.expenses shouldHaveSize 1
          val syncedExpense1 = syncAfterCreate.changedEntities.expenses.first()
          syncedExpense1.id shouldBe createdExpense.id
          syncedExpense1.title shouldBe "Pizza"
          syncedExpense1.amount shouldBe 50.0

          // User A updates the expense
          val updateRequest =
              FakeCreateExpenseRequestFactory.createEqual(
                  userIds = listOf(f.user1.userId, f.user2.userId),
                  payerId = f.user1.userId,
                  title = "Fancy Pizza",
                  amount = 70.0,
              )
          f.client1.updateExpense(f.group.id, createdExpense.id, updateRequest)

          // User B syncs changes since v1
          val syncAfterUpdate = f.client2.sync(sinceVersion = v1)
          val v2 = syncAfterUpdate.latestVersion
          (v2 > v1) shouldBe true
          syncAfterUpdate.changedEntities.expenses shouldHaveSize 1
          val syncedExpense2 = syncAfterUpdate.changedEntities.expenses.first()
          syncedExpense2.id shouldBe createdExpense.id
          syncedExpense2.title shouldBe "Fancy Pizza"
          syncedExpense2.amount shouldBe 70.0

          // User A deletes the expense
          val deleteResponse = f.client1.deleteExpense(f.group.id, createdExpense.id)
          deleteResponse.status shouldBe HttpStatusCode.NoContent

          // User B syncs changes since v2
          val syncAfterDelete = f.client2.sync(sinceVersion = v2)
          val v3 = syncAfterDelete.latestVersion
          (v3 > v2) shouldBe true
          syncAfterDelete.changedEntities.expenses.shouldBeEmpty()
          syncAfterDelete.deletedEntities.expenses shouldHaveSize 1
          syncAfterDelete.deletedEntities.expenses.first() shouldBe createdExpense.id

          // Incremental sync with latest version returns empty changes
          val syncIdle = f.client2.sync(sinceVersion = v3)
          syncIdle.changedEntities.expenses.shouldBeEmpty()
          syncIdle.deletedEntities.expenses.shouldBeEmpty()
        }
      }

      When("an outsider user syncs after another user creates a group and expense") {
        val f by testValue { createGroupWith1MemberFixture() }
        val outsiderClient by testValue { createClient("Outsider") }
        val syncResponse by testValue {
          val createRequest =
              FakeCreateExpenseRequestFactory.createEqual(
                  userIds = listOf(f.user.userId),
                  payerId = f.user.userId,
                  title = "Secret Expense",
                  amount = 100.0,
              )
          f.client.createExpense(f.group.id, createRequest)
          outsiderClient.sync(sinceVersion = 0)
        }
        Then("outsider does not see other users' group expenses") {
          syncResponse.changedEntities.expenses.shouldBeEmpty()
        }
      }
    }
  }
}
