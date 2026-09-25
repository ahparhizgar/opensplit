package com.opensplit.features

import com.opensplit.createClientByToken
import com.opensplit.createGroup
import com.opensplit.dto.auth.AuthResult
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.group.JoinGroupRequest
import com.opensplit.dto.sync.SyncResponse
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

class SyncRoutesTest {

  @Test
  fun syncExpenses_twoUsers_createUpdateDeleteFlow() = testOpenSplit {
    // 1. User A creates group
    val group = client.createGroup()
    val userAId = group.members[0].userId

    // 2. User B signs up and joins group
    val userBAuth =
        client
            .post("/users") { setBody(SignUpRequest("userB@example.com", "password123", "User B")) }
            .body<AuthResult>()

    val userBClient = createClientByToken(userBAuth.accessToken)
    val joinRes =
        userBClient.post("/groups/memberships") { setBody(JoinGroupRequest(group.inviteLink)) }
    assertEquals(HttpStatusCode.OK, joinRes.status)

    // Get initial sync version for User B
    val initialSync = userBClient.get("/sync?sinceVersion=0").body<SyncResponse>()
    val v0 = initialSync.latestVersion

    // 3. User A creates an expense
    val createResponse =
        client.post("/groups/${group.id}/expenses") {
          setBody(
              CreateExpenseRequest(
                  title = "Pizza",
                  amount = 50.0,
                  participants =
                      listOf(
                          ParticipantShareDto(userAId, paidShare = 50.0, consumedShare = 25.0),
                          ParticipantShareDto(
                              userBAuth.userId,
                              paidShare = 0.0,
                              consumedShare = 25.0,
                          ),
                      ),
                  splitMethod = SplitMethod.Equally(listOf(userAId, userBAuth.userId)),
              )
          )
        }
    assertEquals(HttpStatusCode.Created, createResponse.status)
    val createdExpense = createResponse.body<ExpenseDto>()

    // 4. User B syncs changes since v0
    val syncAfterCreate = userBClient.get("/sync?sinceVersion=$v0").body<SyncResponse>()
    val v1 = syncAfterCreate.latestVersion
    assertTrue(v1 > v0, "Latest version should increase after creation")
    assertEquals(1, syncAfterCreate.changedEntities.expenses.size)
    val syncedExpense1 = syncAfterCreate.changedEntities.expenses.first()
    assertEquals(createdExpense.id, syncedExpense1.id)
    assertEquals("Pizza", syncedExpense1.title)
    assertEquals(50.0, syncedExpense1.amount)

    // 5. User A updates the expense
    val updateResponse =
        client.put("/groups/${group.id}/expenses/${createdExpense.id}") {
          setBody(
              CreateExpenseRequest(
                  title = "Fancy Pizza",
                  amount = 70.0,
                  participants =
                      listOf(
                          ParticipantShareDto(userAId, paidShare = 70.0, consumedShare = 35.0),
                          ParticipantShareDto(
                              userBAuth.userId,
                              paidShare = 0.0,
                              consumedShare = 35.0,
                          ),
                      ),
                  splitMethod = SplitMethod.Equally(listOf(userAId, userBAuth.userId)),
              )
          )
        }
    assertEquals(HttpStatusCode.OK, updateResponse.status)

    // 6. User B syncs changes since v1
    val syncAfterUpdate = userBClient.get("/sync?sinceVersion=$v1").body<SyncResponse>()
    val v2 = syncAfterUpdate.latestVersion
    assertTrue(v2 > v1, "Latest version should increase after update")
    assertEquals(1, syncAfterUpdate.changedEntities.expenses.size)
    val syncedExpense2 = syncAfterUpdate.changedEntities.expenses.first()
    assertEquals(createdExpense.id, syncedExpense2.id)
    assertEquals("Fancy Pizza", syncedExpense2.title)
    assertEquals(70.0, syncedExpense2.amount)

    // 7. User A deletes the expense
    val deleteResponse = client.delete("/groups/${group.id}/expenses/${createdExpense.id}")
    assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

    // 8. User B syncs changes since v2
    val syncAfterDelete = userBClient.get("/sync?sinceVersion=$v2").body<SyncResponse>()
    val v3 = syncAfterDelete.latestVersion
    assertTrue(v3 > v2, "Latest version should increase after deletion")
    assertEquals(0, syncAfterDelete.changedEntities.expenses.size)
    assertEquals(1, syncAfterDelete.deletedEntities.expenses.size)
    assertEquals(createdExpense.id, syncAfterDelete.deletedEntities.expenses.first())

    // 9. Incremental sync with latest version returns empty changes
    val syncIdle = userBClient.get("/sync?sinceVersion=$v3").body<SyncResponse>()
    assertEquals(0, syncIdle.changedEntities.expenses.size)
    assertEquals(0, syncIdle.deletedEntities.expenses.size)
  }

  @Test
  fun syncExpenses_groupIsolation() = testOpenSplit {
    // 1. User A creates group & expense
    val group = client.createGroup()
    val userAId = group.members[0].userId

    client.post("/groups/${group.id}/expenses") {
      setBody(
          CreateExpenseRequest(
              title = "Secret Expense",
              amount = 100.0,
              participants =
                  listOf(ParticipantShareDto(userAId, paidShare = 100.0, consumedShare = 100.0)),
              splitMethod = SplitMethod.Equally(listOf(userAId)),
          )
      )
    }

    // 2. User C (outsider) registers and performs sync
    val userCAuth =
        client
            .post("/users") {
              setBody(SignUpRequest("outsider@example.com", "password123", "Outsider"))
            }
            .body<AuthResult>()

    val userCClient = createClientByToken(userCAuth.accessToken)
    val syncResponse = userCClient.get("/sync?sinceVersion=0").body<SyncResponse>()

    // User C should NOT see User A's group expenses
    assertEquals(0, syncResponse.changedEntities.expenses.size)
  }
}
