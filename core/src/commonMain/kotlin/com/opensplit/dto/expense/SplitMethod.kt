package com.opensplit.dto.expense

import kotlinx.serialization.Serializable

@Serializable data class ParticipantAmount(val userId: String, val amount: Double)

@Serializable
sealed interface SplitMethod {

  /**
   * @param totalAmount The total amount of the expense.
   * @param allMembers A set of all participant user IDs.
   * @return A list of ParticipantAmount representing the amounts consumed by each participant.
   */
  fun calculateConsumedAmounts(
      totalAmount: Double,
      allMembers: Set<String>,
  ): List<ParticipantAmount>

  @Serializable
  data class Equally(val userIds: List<String>) : SplitMethod {
    override fun calculateConsumedAmounts(
        totalAmount: Double,
        allMembers: Set<String>,
    ): List<ParticipantAmount> {
      val share = if (userIds.isEmpty()) 0.0 else totalAmount / userIds.size
      return allMembers.map { userId ->
        ParticipantAmount(userId, if (userId in userIds) share else 0.0)
      }
    }
  }

  @Serializable
  data class Unequally(val amounts: Map<String, Double>) : SplitMethod {
    override fun calculateConsumedAmounts(
        totalAmount: Double,
        allMembers: Set<String>,
    ): List<ParticipantAmount> {
      return allMembers.map { userId -> ParticipantAmount(userId, amounts[userId] ?: 0.0) }
    }
  }

  @Serializable
  data class Percentage(val percentages: Map<String, Double>) : SplitMethod {
    override fun calculateConsumedAmounts(
        totalAmount: Double,
        allMembers: Set<String>,
    ): List<ParticipantAmount> {
      return allMembers.map { userId ->
        val amount = (totalAmount * (percentages[userId] ?: 0.0)) / 100.0
        ParticipantAmount(userId, amount)
      }
    }
  }

  @Serializable
  data class Shares(val shares: Map<String, Int>) : SplitMethod {
    override fun calculateConsumedAmounts(
        totalAmount: Double,
        allMembers: Set<String>,
    ): List<ParticipantAmount> {
      val totalShares = shares.values.sum()
      return allMembers.map { userId ->
        val amount =
            if (totalShares == 0) 0.0 else (totalAmount * (shares[userId] ?: 0)) / totalShares
        ParticipantAmount(userId, amount)
      }
    }
  }

  @Serializable
  data class Adjustment(val adjustments: Map<String, Double>) : SplitMethod {
    override fun calculateConsumedAmounts(
        totalAmount: Double,
        allMembers: Set<String>,
    ): List<ParticipantAmount> {
      val totalAdjustments = adjustments.values.sum()
      val remainingAmount = totalAmount - totalAdjustments
      val equalShare = if (allMembers.isEmpty()) 0.0 else remainingAmount / allMembers.size

      return allMembers.map { userId ->
        val adjustment = adjustments[userId] ?: 0.0
        ParticipantAmount(userId, equalShare + adjustment)
      }
    }
  }
}
