package com.opensplit.dto.expense

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SplitMethodEdgeCasesTest : BehaviorSpec() {
  init {
    Given("an expense amount of 100.0 split equally") {
      val totalAmount = 100.0
      val allParticipants = setOf("user1", "user2")

      When("the split list is empty") {
        val method = SplitMethod.Equally(emptyList())
        val result =
            method.calculateConsumedAmounts(totalAmount, allParticipants).associate {
              it.userId to it.amount
            }

        Then("everyone consumes 0.0 instead of dividing by zero") {
          result["user1"] shouldBe 0.0
          result["user2"] shouldBe 0.0
        }
      }

      When("only user1 is in the split list") {
        val method = SplitMethod.Equally(listOf("user1"))
        val result =
            method.calculateConsumedAmounts(totalAmount, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user1 consumes the whole amount and user2 consumes 0.0") {
          result["user1"] shouldBe 100.0
          result["user2"] shouldBe 0.0
        }
      }
    }

    Given("an expense amount of 100.0 split unequally") {
      val totalAmount = 100.0
      val allParticipants = setOf("user1", "user2")

      When("user2 has no entry in the amounts map") {
        val method = SplitMethod.Unequally(mapOf("user1" to 30.0))
        val result =
            method.calculateConsumedAmounts(totalAmount, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user2 consumes 0.0") {
          result["user1"] shouldBe 30.0
          result["user2"] shouldBe 0.0
        }
      }

      When("the amounts map has an entry for a non-participant") {
        val method = SplitMethod.Unequally(mapOf("user1" to 30.0, "user3" to 70.0))
        val result = method.calculateConsumedAmounts(totalAmount, allParticipants)

        Then("only the actual participants get an entry") {
          result.map { it.userId } shouldBe listOf("user1", "user2")
          result.map { it.amount } shouldBe listOf(30.0, 0.0)
        }
      }
    }

    Given("an expense amount of 100.0 split by percentage") {
      val totalAmount = 100.0
      val allParticipants = setOf("user1", "user2")

      When("user2 has no percentage entry") {
        val method = SplitMethod.Percentage(mapOf("user1" to 60.0))
        val result =
            method.calculateConsumedAmounts(totalAmount, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user2 consumes 0.0") {
          result["user1"] shouldBe 60.0
          result["user2"] shouldBe 0.0
        }
      }
    }

    Given("an expense amount of 100.0 split by shares") {
      val totalAmount = 100.0
      val allParticipants = setOf("user1", "user2")

      When("all shares are zero") {
        val method = SplitMethod.Shares(mapOf("user1" to 0, "user2" to 0))
        val result =
            method.calculateConsumedAmounts(totalAmount, allParticipants).associate {
              it.userId to it.amount
            }

        Then("everyone consumes 0.0 instead of dividing by zero") {
          result["user1"] shouldBe 0.0
          result["user2"] shouldBe 0.0
        }
      }

      When("user2 has no shares entry") {
        val method = SplitMethod.Shares(mapOf("user1" to 2))
        val result =
            method.calculateConsumedAmounts(totalAmount, allParticipants).associate {
              it.userId to it.amount
            }

        Then("user1 consumes the whole amount and user2 consumes 0.0") {
          result["user1"] shouldBe 100.0
          result["user2"] shouldBe 0.0
        }
      }
    }

    Given("an expense amount of 100.0 split with adjustments") {
      val totalAmount = 100.0

      When("there are no participants at all") {
        val method = SplitMethod.Adjustment(mapOf("user1" to 20.0))
        val result = method.calculateConsumedAmounts(totalAmount, emptySet())

        Then("the result is empty instead of dividing by zero") { result shouldBe emptyList() }
      }

      When("the adjustments add up to more than the total amount") {
        val allParticipants = setOf("user1", "user2")
        val method = SplitMethod.Adjustment(mapOf("user1" to 120.0))
        val result =
            method.calculateConsumedAmounts(totalAmount, allParticipants).associate {
              it.userId to it.amount
            }

        Then("the negative remainder is split equally on top of the adjustments") {
          result["user1"] shouldBe 110.0
          result["user2"] shouldBe -10.0
        }
      }
    }
  }
}
