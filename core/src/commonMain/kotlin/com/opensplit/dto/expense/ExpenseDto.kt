package com.opensplit.dto.expense

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ExpenseDto(
    val id: String,
    val groupId: String,
    val title: String,
    val amount: Double,
    val creator: String,
    val createdAt: Instant,
    val shares: List<ParticipantShareDto> = emptyList(),
    val splitMethod: SplitMethod,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

@Serializable
data class ParticipantShareDto(
    val userId: String,
    val paidShare: Double,
    val consumedShare: Double,
)

@Serializable
enum class SyncStatus {
  SYNCED,
  PENDING,
}

@Serializable
data class CreateExpenseRequest(
    val title: String,
    val amount: Double,
    val participants: List<ParticipantShareDto>,
    val splitMethod: SplitMethod,
)

@Serializable
enum class SplitType {
  EQUALLY,
  Unequally,
  PERCENTAGE,
  SHARES,
  ADJUSTMENT,
}

object FakeExpenseDtoFactory {
  fun create(
      id: String = "expense-1",
      groupId: String = "group-1",
      title: String = "Dinner",
      amount: Double = 20.0,
      creator: String = "user-1",
      createdAt: Instant = Instant.fromEpochMilliseconds(1700000000000L),
      shares: List<ParticipantShareDto> = FakeParticipantShareDtoFactory.createList(),
      splitMethod: SplitMethod = SplitMethod.Equally(shares.map { it.userId }),
      syncStatus: SyncStatus = SyncStatus.SYNCED,
  ) =
      ExpenseDto(
          id = id,
          groupId = groupId,
          title = title,
          amount = amount,
          creator = creator,
          createdAt = createdAt,
          shares = shares,
          splitMethod = splitMethod,
          syncStatus = syncStatus,
      )

  fun createList() =
      listOf(
          create(),
          create(id = "expense-2", title = "Lunch", amount = 15.0),
      )
}

object FakeParticipantShareDtoFactory {
  fun create(
      userId: String = "user-1",
      paidShare: Double = 0.0,
      consumedShare: Double = 0.0,
  ) = ParticipantShareDto(userId = userId, paidShare = paidShare, consumedShare = consumedShare)

  fun createList() =
      listOf(
          create(userId = "user-1", paidShare = 10.0, consumedShare = 5.0),
          create(userId = "user-2", paidShare = 0.0, consumedShare = 5.0),
      )
}
