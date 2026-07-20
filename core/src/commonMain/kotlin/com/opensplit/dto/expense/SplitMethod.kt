package com.opensplit.dto.expense

import kotlinx.serialization.Serializable

@Serializable data class ParticipantAmount(val userId: String, val amount: Double)

@Serializable
sealed interface SplitMethod {

  /**
   * @param payAmounts A list of ParticipantAmount representing the amounts paid by each
   *   participant.
   * @param allParticipants A set of all participant user IDs.
   * @return A list of ParticipantAmount representing the amounts owed by each participant.
   *   (positive: they should get back, negative: they owe)
   */
  fun calculateOwedAmounts(
      payAmounts: List<ParticipantAmount>,
      allParticipants: Set<String>,
  ): List<ParticipantAmount>

  @Serializable
  data class Equally(val userIds: List<String>) : SplitMethod {
    override fun calculateOwedAmounts(
        payAmounts: List<ParticipantAmount>,
        allParticipants: Set<String>,
    ): List<ParticipantAmount> {
      val totalAmount = payAmounts.sumOf { it.amount }
      val share = if (userIds.isEmpty()) 0.0 else totalAmount / userIds.size
      val shares = allParticipants.associateWith { if (it in userIds) share else 0.0 }
      return calculateBalances(payAmounts, allParticipants, shares)
    }
  }

  @Serializable
  data class Unequally(val amounts: Map<String, Double>) : SplitMethod {
    override fun calculateOwedAmounts(
        payAmounts: List<ParticipantAmount>,
        allParticipants: Set<String>,
    ): List<ParticipantAmount> {
      val shares = allParticipants.associateWith { amounts[it] ?: 0.0 }
      return calculateBalances(payAmounts, allParticipants, shares)
    }
  }

  @Serializable
  data class Percentage(val percentages: Map<String, Double>) : SplitMethod {
    override fun calculateOwedAmounts(
        payAmounts: List<ParticipantAmount>,
        allParticipants: Set<String>,
    ): List<ParticipantAmount> {
      val totalAmount = payAmounts.sumOf { it.amount }
      val shares = allParticipants.associateWith {
        (totalAmount * (percentages[it] ?: 0.0)) / 100.0
      }
      return calculateBalances(payAmounts, allParticipants, shares)
    }
  }

  @Serializable
  data class Shares(val shares: Map<String, Int>) : SplitMethod {
    override fun calculateOwedAmounts(
        payAmounts: List<ParticipantAmount>,
        allParticipants: Set<String>,
    ): List<ParticipantAmount> {
      val totalAmount = payAmounts.sumOf { it.amount }
      val totalShares = shares.values.sum()
      val sharesMap = allParticipants.associateWith {
        if (totalShares == 0) 0.0 else (totalAmount * (shares[it] ?: 0)) / totalShares
      }
      return calculateBalances(payAmounts, allParticipants, sharesMap)
    }
  }

  @Serializable
  data class Adjustment(val adjustments: Map<String, Double>) : SplitMethod {
    override fun calculateOwedAmounts(
        payAmounts: List<ParticipantAmount>,
        allParticipants: Set<String>,
    ): List<ParticipantAmount> {
      val totalAmount = payAmounts.sumOf { it.amount }
      val totalAdjustments = adjustments.values.sum()
      val remainingAmount = totalAmount - totalAdjustments
      val equalShare =
          if (allParticipants.isEmpty()) 0.0 else remainingAmount / allParticipants.size

      val shares = allParticipants.associateWith { userId ->
        val adjustment = adjustments[userId] ?: 0.0
        equalShare + adjustment
      }
      return calculateBalances(payAmounts, allParticipants, shares)
    }
  }
}

private fun calculateBalances(
    payAmounts: List<ParticipantAmount>,
    allParticipants: Set<String>,
    shares: Map<String, Double>,
): List<ParticipantAmount> {
  val paidMap = payAmounts.associate { it.userId to it.amount }
  return allParticipants.map { userId ->
    val paid = paidMap[userId] ?: 0.0
    val share = shares[userId] ?: 0.0
    ParticipantAmount(userId, paid - share)
  }
}
