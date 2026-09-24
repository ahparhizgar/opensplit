package com.opensplit.domain

import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Expense(
    val id: String,
    val groupId: String,
    val title: String,
    val amount: Double,
    val creator: String,
    val createdAt: Instant,
    val participants: List<ParticipantShare>,
    val splitMethod: SplitMethod,
    val syncStatus: SyncStatus,
)

@Serializable
data class ParticipantShare(
    val userId: String,
    val paidShare: Double,
    val consumedShare: Double,
) {
  val netBalance: Double
    get() = paidShare - consumedShare
}

object FakeExpenseFactory {
  fun create(
      id: String = "expense-1",
      groupId: String = "group-1",
      title: String = "Pizza",
      amount: Double = 20.0,
      creator: String = "user-1",
      createdAt: Instant = Instant.fromEpochMilliseconds(1700000000000L),
      participants: List<ParticipantShare> = listOf(FakeParticipantShareFactory.create()),
      splitMethod: SplitMethod = SplitMethod.Equally(participants.map { it.userId }),
      syncStatus: SyncStatus = SyncStatus.SYNCED,
  ) =
      Expense(
          id = id,
          groupId = groupId,
          title = title,
          amount = amount,
          creator = creator,
          createdAt = createdAt,
          participants = participants,
          splitMethod = splitMethod,
          syncStatus = syncStatus,
      )

  fun createList() =
      listOf(
          create(),
          create(id = "expense-2", title = "Burger", amount = 15.0),
          create(id = "expense-3", title = "Soda", amount = 5.0),
      )
}

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
      )

  fun create1() = create("user-1", "Amir Hossein Parhizgar", balance = 10.15, isCurrentUser = true)

  fun create2() = create("user-2", "Abolqasem Ferdowsi", balance = -10.15)

  fun create3() = create("user-3", "Albert Einstein", balance = 0.0)

  fun createList() = listOf(create1(), create2())

  fun createListWith2Members() = listOf(create1(), create2())

  fun createListWith3Members() = listOf(create1(), create2(), create3())
}

object FakeParticipantShareFactory {
  fun create(
      userId: String = "user-1",
      paidShare: Double = 0.0,
      consumedShare: Double = 0.0,
  ) = ParticipantShare(userId, paidShare, consumedShare)
}
