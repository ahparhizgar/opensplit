package com.opensplit.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class DomainPropertiesTest : BehaviorSpec() {
  init {
    Given("a participant share") {
      When("they paid more than they consumed") {
        Then("their net balance is positive") {
          FakeParticipantShareFactory.create(paidShare = 30.0, consumedShare = 10.0)
              .netBalance shouldBe 20.0
        }
      }

      When("they consumed more than they paid") {
        Then("their net balance is negative") {
          FakeParticipantShareFactory.create(paidShare = 0.0, consumedShare = 30.0)
              .netBalance shouldBe -30.0
        }
      }

      When("they paid exactly what they consumed") {
        Then("their net balance is zero") {
          FakeParticipantShareFactory.create(paidShare = 15.0, consumedShare = 15.0)
              .netBalance shouldBe 0.0
        }
      }
    }

    Given("a group") {
      When("its balance is exactly zero") {
        Then("it is settled") { FakeGroupFactory.create(balance = 0.0).isSettled shouldBe true }
      }

      When("its balance is non-zero, even by a fraction") {
        Then("it is not settled") {
          FakeGroupFactory.create(balance = 10.15).isSettled shouldBe false
          FakeGroupFactory.create(balance = -10.15).isSettled shouldBe false
          FakeGroupFactory.create(balance = 0.001).isSettled shouldBe false
        }
      }
    }

    Given("a participant balance") {
      When("it is within half a cent of zero") {
        Then("it counts as settled") {
          FakeParticipantBalanceFactory.create(balance = 0.0).isSettled shouldBe true
          FakeParticipantBalanceFactory.create(balance = 0.004).isSettled shouldBe true
          FakeParticipantBalanceFactory.create(balance = -0.004).isSettled shouldBe true
        }
      }

      When("it is at or beyond the half-cent epsilon") {
        Then("it does not count as settled") {
          FakeParticipantBalanceFactory.create(balance = 0.005).isSettled shouldBe false
          FakeParticipantBalanceFactory.create(balance = -0.005).isSettled shouldBe false
          FakeParticipantBalanceFactory.create(balance = 0.01).isSettled shouldBe false
        }
      }
    }

    Given("a group settlement") {
      When("there are no transfers left") {
        Then("it is settled") {
          FakeGroupSettlementFactory.create(transfers = emptyList()).isSettled shouldBe true
        }
      }

      When("at least one transfer is still outstanding") {
        Then("it is not settled") { FakeGroupSettlementFactory.create().isSettled shouldBe false }
      }
    }

    Given("a group's members with stored balances") {
      When("balances are read from the members") {
        Then("each member's balance is carried over unchanged") {
          SettlementCalculator.balancesFromMembers(FakeMemberFactory.createList()) shouldBe
              listOf(
                  FakeParticipantBalanceFactory.create(userId = "user-1", balance = 10.15),
                  FakeParticipantBalanceFactory.create(userId = "user-2", balance = -10.15),
              )
        }
      }
    }
  }
}
