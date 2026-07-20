package com.opensplit.domain.expense

object SplitCalculator {

  fun calculateEqual(amount: Double, userIds: List<String>): Map<String, Double> {
    if (userIds.isEmpty()) return emptyMap()
    val share = amount / userIds.size
    return userIds.associateWith { share }
  }

  fun calculatePercentage(amount: Double, percentages: Map<String, Double>): Map<String, Double> {
    return percentages.mapValues { (_, percentage) -> (amount * percentage) / 100.0 }
  }

  fun calculateShares(amount: Double, shares: Map<String, Double>): Map<String, Double> {
    val totalShares = shares.values.sum()
    if (totalShares == 0.0) return emptyMap()
    return shares.mapValues { (_, share) -> (amount * share) / totalShares }
  }

  fun calculateAdjustment(
      amount: Double,
      adjustments: Map<String, Double>,
      userIds: List<String>,
  ): Map<String, Double> {
    if (userIds.isEmpty()) return emptyMap()
    val totalAdjustments = adjustments.values.sum()
    val remainingAmount = amount - totalAdjustments
    val equalShare = remainingAmount / userIds.size

    return userIds.associateWith { userId ->
      val adjustment = adjustments[userId] ?: 0.0
      equalShare + adjustment
    }
  }

  fun calculateExact(shares: Map<String, Double>): Map<String, Double> {
    return shares
  }
}
