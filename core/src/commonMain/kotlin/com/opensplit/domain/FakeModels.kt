package com.opensplit.domain

import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import kotlin.time.Instant

object FakeMemberFactory {
  fun create(
      userId: String = "user-id",
      name: String = "User $userId",
      email: String = "$userId@example.com",
      isOwner: Boolean = false,
      isCurrentUser: Boolean = false,
      balance: Double = 0.0,
      balanceCurrency: String = "IRR",
  ) =
      Member(
          userId = userId,
          name = name,
          email = email,
          isOwner = isOwner,
          isCurrentUser = isCurrentUser,
          balance = balance,
          balanceCurrency = balanceCurrency,
      )

  fun create1() = create("user-1", "Amir Hossein Parhizgar", balance = 10.15, isCurrentUser = true)

  fun create2() = create("user-2", "Abolqasem Ferdowsi", balance = -10.15)

  fun create3() = create("user-3", "Albert Einstein", balance = 0.0)

  fun createList() = listOf(create1(), create2())

  fun createListWith2Members() = listOf(create1(), create2())

  fun createListWith3Members() = listOf(create1(), create2(), create3())
}

object FakeHouseholdFactory {
  fun create(
      id: String = "household-1",
      name: String = "My Household",
      members: List<Member> = FakeMemberFactory.createList(),
      isOwner: Boolean = false,
      inviteLink: String = "https://opensplit.com/invite/85243892",
      balance: Double = 0.0,
  ) =
      Household(
          id = id,
          name = name,
          members = members,
          isOwner = isOwner,
          inviteLink = inviteLink,
          balance = balance,
      )
}

object FakeParticipantShareFactory {
  fun create(
      userId: String = "user-1",
      paidShare: Double = 0.0,
      owedShare: Double = 0.0,
      netBalance: Double = paidShare - owedShare,
  ) = ParticipantShare(userId, paidShare, owedShare, netBalance)
}

object FakeExpenseFactory {
  fun create(
      id: String = "expense-1",
      householdId: String = "household-1",
      title: String = "Pizza",
      amount: Double = 20.0,
      payerId: String = "user-1",
      createdAt: Instant = Instant.fromEpochMilliseconds(1700000000000L),
      participants: List<ParticipantShare> = listOf(FakeParticipantShareFactory.create()),
      splitMethod: SplitMethod = SplitMethod.Equally(participants.map { it.userId }),
      syncStatus: SyncStatus = SyncStatus.SYNCED,
  ) =
      Expense(
          id = id,
          householdId = householdId,
          title = title,
          amount = amount,
          payerId = payerId,
          createdAt = createdAt,
          participants = participants,
          splitMethod = splitMethod,
          syncStatus = syncStatus,
      )
}
