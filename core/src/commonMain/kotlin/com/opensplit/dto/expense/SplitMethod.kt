package com.opensplit.dto.expense

import kotlinx.serialization.Serializable

@Serializable data class ParticipantAmount(val userId: String, val amount: Double)

@Serializable
sealed interface SplitMethod {
  val type: SplitType

  fun calculateOwedAmounts(totalAmount: Double): List<ParticipantAmount>

  @Serializable
  data class Equally(val userIds: List<String>) : SplitMethod {
    override val type = SplitType.EQUALLY

    override fun calculateOwedAmounts(totalAmount: Double): List<ParticipantAmount> {
      if (userIds.isEmpty()) return emptyList()
      val share = totalAmount / userIds.size
      return userIds.map { ParticipantAmount(it, share) }
    }
  }

  @Serializable
  data class Unequally(val amounts: Map<String, Double>) : SplitMethod {
    override val type = SplitType.EXACT

    override fun calculateOwedAmounts(totalAmount: Double): List<ParticipantAmount> = amounts.map {
      ParticipantAmount(it.key, it.value)
    }
  }

  @Serializable
  data class Percentage(val percentages: Map<String, Double>) : SplitMethod {
    override val type = SplitType.PERCENTAGE

    override fun calculateOwedAmounts(totalAmount: Double): List<ParticipantAmount> =
        percentages.map {
          ParticipantAmount(it.key, (totalAmount * it.value) / 100.0)
        }
  }

  @Serializable
  data class Shares(val shares: Map<String, Double>) : SplitMethod {
    override val type = SplitType.SHARES

    override fun calculateOwedAmounts(totalAmount: Double): List<ParticipantAmount> {
      val totalShares = shares.values.sum()
      if (totalShares == 0.0) return emptyList()
      return shares.map { ParticipantAmount(it.key, (totalAmount * it.value) / totalShares) }
    }
  }

  @Serializable
  data class Adjustment(val adjustments: Map<String, Double>, val equallyUserIds: List<String>) :
      SplitMethod {
    override val type = SplitType.ADJUSTMENT

    override fun calculateOwedAmounts(totalAmount: Double): List<ParticipantAmount> {
      if (equallyUserIds.isEmpty()) return emptyList()
      val totalAdjustments = adjustments.values.sum()
      val remainingAmount = totalAmount - totalAdjustments
      val equalShare = remainingAmount / equallyUserIds.size

      return equallyUserIds.map { userId ->
        val adjustment = adjustments[userId] ?: 0.0
        ParticipantAmount(userId, equalShare + adjustment)
      }
    }
  }
}
