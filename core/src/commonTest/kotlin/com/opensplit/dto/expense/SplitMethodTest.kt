package com.opensplit.dto.expense

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SplitMethodTest :
    BehaviorSpec({
      Given("an expense amount of 100.0") {
        val amount = 100.0

        When("splitting equally between 2 users") {
          val userIds = listOf("user1", "user2")
          val method = SplitMethod.Equally(userIds)
          val result = method.calculateOwedAmounts(amount).associate { it.userId to it.amount }

          Then("each user should owe 50.0") {
            result["user1"] shouldBe 50.0
            result["user2"] shouldBe 50.0
          }
        }

        When("splitting equally between 3 users") {
          val userIds = listOf("user1", "user2", "user3")
          val method = SplitMethod.Equally(userIds)
          val result = method.calculateOwedAmounts(amount)

          Then("it should handle rounding (roughly)") {
            result.sumOf { it.amount } shouldBe 100.0
          }
        }
      }

      Given("an expense amount of 100.0 for percentage split") {
        val amount = 100.0

        When("splitting by 60% and 40%") {
          val percentages = mapOf("user1" to 60.0, "user2" to 40.0)
          val method = SplitMethod.Percentage(percentages)
          val result = method.calculateOwedAmounts(amount).associate { it.userId to it.amount }

          Then("user1 should owe 60.0 and user2 should owe 40.0") {
            result["user1"] shouldBe 60.0
            result["user2"] shouldBe 40.0
          }
        }
      }

      Given("an expense amount of 100.0 for unequally split") {
        val amount = 100.0

        When("user1 owes 30.0 and user2 owes 70.0") {
          val amounts = mapOf("user1" to 30.0, "user2" to 70.0)
          val method = SplitMethod.Unequally(amounts)
          val result = method.calculateOwedAmounts(amount).associate { it.userId to it.amount }

          Then("the results should match the provided amounts") {
            result["user1"] shouldBe 30.0
            result["user2"] shouldBe 70.0
          }
        }
      }

      Given("an expense amount of 100.0 for shares split") {
        val amount = 100.0

        When("user1 has 2 shares and user2 has 3 shares") {
          val shares = mapOf("user1" to 2.0, "user2" to 3.0)
          val method = SplitMethod.Shares(shares)
          val result = method.calculateOwedAmounts(amount).associate { it.userId to it.amount }

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
          val method = SplitMethod.Adjustment(adjustments, userIds)
          val result = method.calculateOwedAmounts(amount).associate { it.userId to it.amount }

          Then("user1 should owe 60.0 and user2 should owe 40.0") {
            result["user1"] shouldBe 60.0
            result["user2"] shouldBe 40.0
          }
        }
      }
    })
