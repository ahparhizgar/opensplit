package com.opensplit.e2e

import com.opensplit.dto.expense.FakeCreateExpenseRequestFactory
import com.opensplit.fixture.createExpense
import com.opensplit.fixture.createGroupWith1MemberFixture
import com.opensplit.fixture.createGroupWith2MembersFixture
import com.opensplit.fixture.deleteExpense
import com.opensplit.fixture.sync
import com.opensplit.fixture.updateExpense
import com.opensplit.util.createClient
import com.opensplit.util.testOpenSplit
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncRoutesTest {

  @Test
  fun syncExpenses_twoUsers_createUpdateDeleteFlow() = testOpenSplit {
    // 1 & 2. Create group with 2 members
    val f = createGroupWith2MembersFixture()

    // Get initial sync version for User B
    val initialSync = f.client2.sync(sinceVersion = 0)
    val v0 = initialSync.latestVersion

    // 3. User A creates an expense
    val createRequest =
        FakeCreateExpenseRequestFactory.createEqual(
            userIds = listOf(f.user1.userId),
            payerId = f.user1.userId,
            title = "Pizza",
            amount = 50.0,
        )
    val createdExpense = f.client1.createExpense(f.group.id, createRequest)

    // 4. User B syncs changes since v0
    val syncAfterCreate = f.client2.sync(sinceVersion = v0)
    val v1 = syncAfterCreate.latestVersion
    assertTrue(v1 > v0, "Latest version should increase after creation")
    assertEquals(1, syncAfterCreate.changedEntities.expenses.size)
    val syncedExpense1 = syncAfterCreate.changedEntities.expenses.first()
    assertEquals(createdExpense.id, syncedExpense1.id)
    assertEquals("Pizza", syncedExpense1.title)
    assertEquals(50.0, syncedExpense1.amount)

    // 5. User A updates the expense
    val updateRequest =
        FakeCreateExpenseRequestFactory.createEqual(
            userIds = listOf(f.user1.userId, f.user2.userId),
            payerId = f.user1.userId,
            title = "Fancy Pizza",
            amount = 70.0,
        )
    f.client1.updateExpense(f.group.id, createdExpense.id, updateRequest)

    // 6. User B syncs changes since v1
    val syncAfterUpdate = f.client2.sync(sinceVersion = v1)
    val v2 = syncAfterUpdate.latestVersion
    assertTrue(v2 > v1, "Latest version should increase after update")
    assertEquals(1, syncAfterUpdate.changedEntities.expenses.size)
    val syncedExpense2 = syncAfterUpdate.changedEntities.expenses.first()
    assertEquals(createdExpense.id, syncedExpense2.id)
    assertEquals("Fancy Pizza", syncedExpense2.title)
    assertEquals(70.0, syncedExpense2.amount)

    // 7. User A deletes the expense
    val deleteResponse = f.client1.deleteExpense(f.group.id, createdExpense.id)
    assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

    // 8. User B syncs changes since v2
    val syncAfterDelete = f.client2.sync(sinceVersion = v2)
    val v3 = syncAfterDelete.latestVersion
    assertTrue(v3 > v2, "Latest version should increase after deletion")
    assertEquals(0, syncAfterDelete.changedEntities.expenses.size)
    assertEquals(1, syncAfterDelete.deletedEntities.expenses.size)
    assertEquals(createdExpense.id, syncAfterDelete.deletedEntities.expenses.first())

    // 9. Incremental sync with latest version returns empty changes
    val syncIdle = f.client2.sync(sinceVersion = v3)
    assertEquals(0, syncIdle.changedEntities.expenses.size)
    assertEquals(0, syncIdle.deletedEntities.expenses.size)
  }

  @Test
  fun syncExpenses_groupIsolation() = testOpenSplit {
    // 1. User A creates group & expense
    val f = createGroupWith1MemberFixture()
    val createRequest =
        FakeCreateExpenseRequestFactory.createEqual(
            userIds = listOf(f.user.userId),
            payerId = f.user.userId,
            title = "Secret Expense",
            amount = 100.0,
        )
    f.client.createExpense(f.group.id, createRequest)

    // 2. User C (outsider) registers and performs sync
    val outsiderClient = createClient("Outsider")
    val syncResponse = outsiderClient.sync(sinceVersion = 0)

    // User C should NOT see User A's group expenses
    assertEquals(0, syncResponse.changedEntities.expenses.size)
  }
}
