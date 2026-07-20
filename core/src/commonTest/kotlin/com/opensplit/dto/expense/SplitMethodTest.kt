package com.opensplit.dto.expense

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SplitMethodTest : BehaviorSpec() {
  init {
    Given("an expense amount of 100.0") {
      val payAmounts = listOf(ParticipantAmount("user1", 100.0))
      val allParticipants = setOf("user1", "user2")

      When("splitting equally between 2 users") {
        val method = SplitMethod.Equally(listOf("user1", "user2"))
        val result =
            method.calculateOwedAmounts(payAmounts, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user1 should get back 50.0 and user2 should owe 50.0") {
          result["user1"] shouldBe 50.0
          result["user2"] shouldBe -50.0
        }
      }

      When("splitting equally between 3 users") {
        val all3Participants = setOf("user1", "user2", "user3")
        val method = SplitMethod.Equally(all3Participants.toList())
        val result = method.calculateOwedAmounts(payAmounts, all3Participants)

        Then("balances should sum to 0.0") { result.sumOf { it.amount } shouldBe 0.0 }
      }
    }

    Given("an expense amount of 100.0 for percentage split") {
      val payAmounts = listOf(ParticipantAmount("user1", 100.0))
      val allParticipants = setOf("user1", "user2")

      When("splitting by 60% and 40%") {
        val percentages = mapOf("user1" to 60.0, "user2" to 40.0)
        val method = SplitMethod.Percentage(percentages)
        val result =
            method.calculateOwedAmounts(payAmounts, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user1 should get back 40.0 and user2 should owe 40.0") {
          result["user1"] shouldBe 40.0
          result["user2"] shouldBe -40.0
        }
      }
    }

    Given("an expense amount of 100.0 for unequally split") {
      val payAmounts = listOf(ParticipantAmount("user1", 100.0))
      val allParticipants = setOf("user1", "user2")

      When("user1 owes 30.0 and user2 owes 70.0") {
        val amounts = mapOf("user1" to 30.0, "user2" to 70.0)
        val method = SplitMethod.Unequally(amounts)
        val result =
            method.calculateOwedAmounts(payAmounts, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user1 should get back 70.0 and user2 should owe 70.0") {
          result["user1"] shouldBe 70.0
          result["user2"] shouldBe -70.0
        }
      }
    }

    Given("an expense amount of 100.0 for shares split") {
      val payAmounts = listOf(ParticipantAmount("user1", 100.0))
      val allParticipants = setOf("user1", "user2")

      When("user1 has 2 shares and user2 has 3 shares") {
        val shares = mapOf("user1" to 2, "user2" to 3)
        val method = SplitMethod.Shares(shares)
        val result =
            method.calculateOwedAmounts(payAmounts, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user1 should get back 60.0 and user2 should owe 60.0") {
          result["user1"] shouldBe 60.0
          result["user2"] shouldBe -60.0
        }
      }
    }

    Given("an expense amount of 100.0 for adjustment split") {
      val payAmounts = listOf(ParticipantAmount("user1", 100.0))
      val allParticipants = setOf("user1", "user2")

      When("user1 has +20.0 adjustment and user2 has no adjustment") {
        val adjustments = mapOf("user1" to 20.0)
        val method = SplitMethod.Adjustment(adjustments)
        val result =
            method.calculateOwedAmounts(payAmounts, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user1 should get back 40.0 and user2 should owe 40.0") {
          result["user1"] shouldBe 40.0
          result["user2"] shouldBe -40.0
        }
      }
    }
  }
}
