package com.opensplit.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SettlementCalculatorTest : BehaviorSpec() {
  init {
    Given("an expense of 90.0 paid by user-1 and split equally between 3 participants") {
      val expenses =
          listOf(
              FakeExpenseFactory.create(
                  amount = 90.0,
                  creator = "user-1",
                  participants =
                      listOf(
                          FakeParticipantShareFactory.create(
                              userId = "user-1",
                              paidShare = 90.0,
                              consumedShare = 30.0,
                          ),
                          FakeParticipantShareFactory.create(
                              userId = "user-2",
                              paidShare = 0.0,
                              consumedShare = 30.0,
                          ),
                          FakeParticipantShareFactory.create(
                              userId = "user-3",
                              paidShare = 0.0,
                              consumedShare = 30.0,
                          ),
                      ),
              )
          )

      When("balances are calculated") {
        val balances = SettlementCalculator.balancesFromExpenses(expenses)

        Then("user-1 is owed 60.0 and the others owe 30.0 each") {
          balances shouldBe
              listOf(
                  FakeParticipantBalanceFactory.create(userId = "user-1", balance = 60.0),
                  FakeParticipantBalanceFactory.create(userId = "user-2", balance = -30.0),
                  FakeParticipantBalanceFactory.create(userId = "user-3", balance = -30.0),
              )
        }

        Then("each debtor pays user-1 directly") {
          SettlementCalculator.settleUp(balances) shouldBe
              listOf(
                  FakeSettlementTransferFactory.create(
                      fromUserId = "user-2",
                      toUserId = "user-1",
                      amount = 30.0,
                  ),
                  FakeSettlementTransferFactory.create(
                      fromUserId = "user-3",
                      toUserId = "user-1",
                      amount = 30.0,
                  ),
              )
        }
      }
    }

    Given("an expense of 100.0 paid by user-1 with uneven shares") {
      val expenses =
          listOf(
              FakeExpenseFactory.create(
                  amount = 100.0,
                  creator = "user-1",
                  participants =
                      listOf(
                          FakeParticipantShareFactory.create(
                              userId = "user-1",
                              paidShare = 100.0,
                              consumedShare = 30.0,
                          ),
                          FakeParticipantShareFactory.create(
                              userId = "user-2",
                              paidShare = 0.0,
                              consumedShare = 70.0,
                          ),
                      ),
              )
          )

      When("the group settlement is calculated") {
        val settlement = SettlementCalculator.forExpenses("group-1", expenses)

        Then("there is a single transfer of 70.0 from user-2 to user-1") {
          settlement.transfers shouldBe
              listOf(
                  FakeSettlementTransferFactory.create(
                      fromUserId = "user-2",
                      toUserId = "user-1",
                      amount = 70.0,
                  )
              )
          settlement.isSettled shouldBe false
        }
      }
    }

    Given("user-1 paid for two expenses and user-2 paid for one") {
      val expenses =
          listOf(
              FakeExpenseFactory.create(
                  id = "expense-1",
                  amount = 60.0,
                  creator = "user-1",
                  participants =
                      listOf(
                          FakeParticipantShareFactory.create(
                              userId = "user-1",
                              paidShare = 60.0,
                              consumedShare = 30.0,
                          ),
                          FakeParticipantShareFactory.create(
                              userId = "user-2",
                              paidShare = 0.0,
                              consumedShare = 30.0,
                          ),
                      ),
              ),
              FakeExpenseFactory.create(
                  id = "expense-2",
                  amount = 40.0,
                  creator = "user-1",
                  participants =
                      listOf(
                          FakeParticipantShareFactory.create(
                              userId = "user-1",
                              paidShare = 40.0,
                              consumedShare = 20.0,
                          ),
                          FakeParticipantShareFactory.create(
                              userId = "user-2",
                              paidShare = 0.0,
                              consumedShare = 20.0,
                          ),
                      ),
              ),
              FakeExpenseFactory.create(
                  id = "expense-3",
                  amount = 20.0,
                  creator = "user-2",
                  participants =
                      listOf(
                          FakeParticipantShareFactory.create(
                              userId = "user-1",
                              paidShare = 0.0,
                              consumedShare = 10.0,
                          ),
                          FakeParticipantShareFactory.create(
                              userId = "user-2",
                              paidShare = 20.0,
                              consumedShare = 10.0,
                          ),
                      ),
              ),
          )

      When("balances are calculated across all expenses") {
        val balances = SettlementCalculator.balancesFromExpenses(expenses)

        Then("payments and consumptions are netted per participant") {
          balances shouldBe
              listOf(
                  FakeParticipantBalanceFactory.create(userId = "user-1", balance = 40.0),
                  FakeParticipantBalanceFactory.create(userId = "user-2", balance = -40.0),
              )
        }

        Then("a single transfer of 40.0 settles the group") {
          SettlementCalculator.settleUp(balances) shouldBe
              listOf(
                  FakeSettlementTransferFactory.create(
                      fromUserId = "user-2",
                      toUserId = "user-1",
                      amount = 40.0,
                  )
              )
        }
      }
    }

    Given("balances of four participants that could settle in many pairwise transfers") {
      val balances =
          listOf(
              FakeParticipantBalanceFactory.create(userId = "user-1", balance = 50.0),
              FakeParticipantBalanceFactory.create(userId = "user-2", balance = 30.0),
              FakeParticipantBalanceFactory.create(userId = "user-3", balance = -40.0),
              FakeParticipantBalanceFactory.create(userId = "user-4", balance = -40.0),
          )

      When("transfers are simplified") {
        val transfers = SettlementCalculator.settleUp(balances)

        Then("largest debts are matched with largest credits in at most n-1 transfers") {
          transfers shouldBe
              listOf(
                  FakeSettlementTransferFactory.create(
                      fromUserId = "user-3",
                      toUserId = "user-1",
                      amount = 40.0,
                  ),
                  FakeSettlementTransferFactory.create(
                      fromUserId = "user-4",
                      toUserId = "user-1",
                      amount = 10.0,
                  ),
                  FakeSettlementTransferFactory.create(
                      fromUserId = "user-4",
                      toUserId = "user-2",
                      amount = 30.0,
                  ),
              )
        }
      }
    }

    Given("a group where everyone already paid exactly their own share") {
      val group = FakeGroupFactory.create(members = FakeMemberFactory.createListWith3Members())
      val expenses =
          listOf(
              FakeExpenseFactory.create(
                  amount = 30.0,
                  participants =
                      listOf(
                          FakeParticipantShareFactory.create(
                              userId = "user-1",
                              paidShare = 30.0,
                              consumedShare = 30.0,
                          )
                      ),
              )
          )

      When("the group settlement is calculated") {
        val settlement = SettlementCalculator.forGroup(group, expenses)

        Then("all members are listed with zero balance and no transfers are needed") {
          settlement.balances shouldBe
              listOf(
                  FakeParticipantBalanceFactory.create(userId = "user-1", balance = 0.0),
                  FakeParticipantBalanceFactory.create(userId = "user-2", balance = 0.0),
                  FakeParticipantBalanceFactory.create(userId = "user-3", balance = 0.0),
              )
          settlement.transfers shouldBe emptyList()
          settlement.isSettled shouldBe true
        }
      }

      When("there are no expenses at all") {
        val settlement = SettlementCalculator.forExpenses("group-1", emptyList())

        Then("the settlement is empty and settled") {
          settlement.balances shouldBe emptyList()
          settlement.transfers shouldBe emptyList()
          settlement.isSettled shouldBe true
        }
      }
    }
  }
}
