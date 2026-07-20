package com.opensplit.domain.expense

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SplitCalculatorTest :
    BehaviorSpec({
      Given("an expense amount of 100.0") {
        val amount = 100.0

        When("splitting equally between 2 users") {
          val userIds = listOf("user1", "user2")
          val result = SplitCalculator.calculateEqual(amount, userIds)

          Then("each user should owe 50.0") {
            result["user1"] shouldBe 50.0
            result["user2"] shouldBe 50.0
          }
        }

        When("splitting equally between 3 users") {
          val userIds = listOf("user1", "user2", "user3")
          val result = SplitCalculator.calculateEqual(amount, userIds)

          Then("it should handle rounding (roughly)") {
            // In a real app, we might want to handle pennies specifically
            // For now, let's assume Double division
            result.values.sum() shouldBe 100.0
          }
        }
      }

      Given("an expense amount of 100.0 for percentage split") {
        val amount = 100.0

        When("splitting by 60% and 40%") {
          val percentages = mapOf("user1" to 60.0, "user2" to 40.0)
          val result = SplitCalculator.calculatePercentage(amount, percentages)

          Then("user1 should owe 60.0 and user2 should owe 40.0") {
            result["user1"] shouldBe 60.0
            result["user2"] shouldBe 40.0
          }
        }
      }

      Given("an expense amount of 100.0 for shares split") {
        val amount = 100.0

        When("user1 has 2 shares and user2 has 3 shares") {
          val shares = mapOf("user1" to 2.0, "user2" to 3.0)
          val result = SplitCalculator.calculateShares(amount, shares)

          Then("user1 should owe 40.0 and user2 should owe 60.0") {
            result["user1"] shouldBe 40.0
            result["user2"] shouldBe 60.0
          }
        }
      }

      Given("an expense amount of 100.0 for adjustment split") {
        val amount = 100.0

        When("user1 has +20.0 adjustment and user2 has no adjustment") {
          val adjustments = mapOf("user1" to 20.0)
          val userIds = listOf("user1", "user2")
          val result = SplitCalculator.calculateAdjustment(amount, adjustments, userIds)

          Then("user1 should owe 60.0 and user2 should owe 40.0") {
            // (100 - 20) / 2 = 40. User1: 40 + 20 = 60. User2: 40.
            result["user1"] shouldBe 60.0
            result["user2"] shouldBe 40.0
          }
        }
      }
    })
