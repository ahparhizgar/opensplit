package com.opensplit

import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.FakeCreateExpenseRequestFactory
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.server.testing.ApplicationTestBuilder

suspend fun HttpClient.createExpenseRequest(
    groupId: String,
    request: CreateExpenseRequest,
): HttpResponse = post("/groups/$groupId/expenses") { setBody(request) }

suspend fun HttpClient.createExpense(
    groupId: String,
    request: CreateExpenseRequest,
): ExpenseDto = createExpenseRequest(groupId, request).body<ExpenseDto>()

suspend fun HttpClient.createExpense(
    groupId: String,
    title: String = "Pizza",
    amount: Double = 25.0,
    participants: List<ParticipantShareDto> = emptyList(),
    splitMethod: SplitMethod = SplitMethod.Equally(participants.map { it.userId }),
): ExpenseDto =
    createExpense(
        groupId = groupId,
        request =
            FakeCreateExpenseRequestFactory.create(
                title = title,
                amount = amount,
                participants = participants,
                splitMethod = splitMethod,
            ),
    )

suspend fun HttpClient.updateExpenseRequest(
    groupId: String,
    expenseId: String,
    request: CreateExpenseRequest,
): HttpResponse = put("/groups/$groupId/expenses/$expenseId") { setBody(request) }

suspend fun HttpClient.updateExpense(
    groupId: String,
    expenseId: String,
    request: CreateExpenseRequest,
): ExpenseDto = updateExpenseRequest(groupId, expenseId, request).body<ExpenseDto>()

suspend fun HttpClient.updateExpense(
    groupId: String,
    expenseId: String,
    title: String,
    amount: Double,
    participants: List<ParticipantShareDto>,
    splitMethod: SplitMethod = SplitMethod.Equally(participants.map { it.userId }),
): ExpenseDto =
    updateExpense(
        groupId = groupId,
        expenseId = expenseId,
        request =
            FakeCreateExpenseRequestFactory.create(
                title = title,
                amount = amount,
                participants = participants,
                splitMethod = splitMethod,
            ),
    )

suspend fun HttpClient.getExpenses(groupId: String): List<ExpenseDto> =
    get("/groups/$groupId/expenses").body<List<ExpenseDto>>()

suspend fun HttpClient.deleteExpense(groupId: String, expenseId: String): HttpResponse =
    delete("/groups/$groupId/expenses/$expenseId")

suspend fun ApplicationTestBuilder.createExpenseWith1MemberFixture(
    title: String = "Pizza",
    amount: Double = 25.0,
): ExpenseWith1MemberFixture {
  val groupFixture = createGroupWith1MemberFixture()
  val request =
      FakeCreateExpenseRequestFactory.createEqual(
          userIds = listOf(groupFixture.user.userId),
          title = title,
          amount = amount,
      )
  val expense = groupFixture.client.createExpense(groupFixture.group.id, request)
  return ExpenseWith1MemberFixture(
      groupFixture = groupFixture,
      expense = expense,
  )
}

suspend fun ApplicationTestBuilder.createExpenseWith2MembersFixture(
    title: String = "Groceries",
    amount: Double = 100.0,
): ExpenseAndMembersFixture =
    createExpenseWith2MembersFixture(
        groupFixture = createGroupWith2MembersFixture(),
        title = title,
        amount = amount,
    )

suspend fun createExpenseWith2MembersFixture(
    groupFixture: GroupAnd2MembersFixture,
    title: String = "Groceries",
    amount: Double = 100.0,
): ExpenseAndMembersFixture {
  val request =
      FakeCreateExpenseRequestFactory.createEqual(
          userIds = listOf(groupFixture.user1.userId, groupFixture.user2.userId),
          payerId = groupFixture.user1.userId,
          title = title,
          amount = amount,
      )
  val expense = groupFixture.client1.createExpense(groupFixture.group.id, request)
  return ExpenseAndMembersFixture(
      groupFixture = groupFixture,
      expense = expense,
  )
}

class ExpenseWith1MemberFixture(
    val groupFixture: GroupWith1MemberFixture,
    val expense: ExpenseDto,
) {
  val group
    get() = groupFixture.group

  val user
    get() = groupFixture.user

  val client
    get() = groupFixture.client
}

class ExpenseAndMembersFixture(
    val groupFixture: GroupAnd2MembersFixture,
    val expense: ExpenseDto,
) {
  val group
    get() = groupFixture.group

  val user1
    get() = groupFixture.user1

  val user2
    get() = groupFixture.user2

  val client1
    get() = groupFixture.client1

  val client2
    get() = groupFixture.client2
}
