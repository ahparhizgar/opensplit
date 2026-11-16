package com.opensplit.integration.component

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.opensplit.component.TestCContext
import com.opensplit.domain.ParticipantShare
import com.opensplit.domain.SettlementTransfer
import com.opensplit.dto.expense.SplitMethod.Equally
import com.opensplit.integration.group.details.GroupDetailsComponent
import com.opensplit.integration.group.details.GroupDetailsComponentFactory
import com.opensplit.repository.ExpenseRepository
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@OptIn(ExperimentalDecomposeApi::class)
class GroupDetailsComponentTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a GroupDetailsComponent") {
      val cContext by testValue { TestCContext().resumed() }
      val detailsComponent by testValue {
        koin
            .get<GroupDetailsComponentFactory>()
            .create(
                cContext,
                GroupDetailsComponent.Config("group-1"),
            )
      }
      When("an expense is created where one participant paid for both") {
        beforeEach {
          koin
              .get<ExpenseRepository>()
              .createExpense(
                  groupId = "group-1",
                  title = "Dinner",
                  amount = 20.0,
                  creator = "user-1",
                  shares =
                      listOf(
                          ParticipantShare("user-1", paidShare = 20.0, consumedShare = 10.0),
                          ParticipantShare("user-2", paidShare = 0.0, consumedShare = 10.0),
                      ),
                  splitMethod = Equally(listOf("user-1", "user-2")),
              )
          testCoroutineScheduler.advanceUntilIdle()
          detailsComponent.uiState.value.expenses.shouldNotBeEmpty()
          detailsComponent.uiState.value.settlement shouldNotBe null
          testCoroutineScheduler.advanceUntilIdle()
        }
        Then("the settlement shows the other participant paying back half") {
          detailsComponent.uiState.value.settlement?.transfers shouldBe
              listOf(
                  SettlementTransfer(fromUserId = "user-2", toUserId = "user-1", amount = 10.0),
              )
        }
      }
    }
  }
}
