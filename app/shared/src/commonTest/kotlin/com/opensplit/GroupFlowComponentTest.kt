package com.opensplit

import androidx.window.core.layout.WindowSizeClass
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.opensplit.component.TestCContext
import com.opensplit.db.GroupDao
import com.opensplit.db.GroupEntity
import com.opensplit.domain.FakeExpenseFactory
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.group.FakeGroupDtoFactory
import com.opensplit.features.group.details.GroupFlowComponent
import com.opensplit.features.group.details.GroupFlowComponentFactory
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.GroupRepository
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Instant

@OptIn(ExperimentalDecomposeApi::class)
class GroupFlowComponentTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a GroupFlowComponent") {
      val cContext by testValue { TestCContext().resumed() }
      val flowComponent by testValue {
        koin
            .get<GroupFlowComponentFactory>()
            .create(
                cContext,
                GroupFlowComponent.Config("group-1"),
            )
      }

      When("mode is set to DUAL initially") {
        beforeEach {
          cContext.windowSizeHolder.update(WindowSizeClass(840, 600))
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("initial details panel is empty by default") {
          flowComponent.panels.value.details.shouldBeNull()
        }

        And("switching mode to SINGLE") {
          beforeEach {
            cContext.windowSizeHolder.update(WindowSizeClass(400, 600))
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("details panel remains empty and single panel shows GroupDetails") {
            flowComponent.panels.value.details.shouldBeNull()
          }
        }
      }

      When("clicking settings button with user intent in SINGLE mode") {
        beforeEach {
          flowComponent.setMode(ChildPanelsMode.SINGLE)
          val mainChild = flowComponent.panels.value.main.instance
          mainChild.onSettingsClick()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("details panel is active with isUserRequested=true") {
          val detailsConfig = flowComponent.panels.value.details?.configuration
          detailsConfig.shouldNotBeNull()
          detailsConfig.shouldBeInstanceOf<GroupFlowComponent.DetailsConfig.Settings>()
          detailsConfig.isUserRequested shouldBe true
        }

        And("switching mode to DUAL and back to SINGLE") {
          beforeEach {
            flowComponent.setMode(ChildPanelsMode.DUAL)
            flowComponent.setMode(ChildPanelsMode.SINGLE)
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("GroupSettings stays active in SINGLE mode because it had user intent") {
            val detailsConfig = flowComponent.panels.value.details?.configuration
            detailsConfig.shouldNotBeNull()
            detailsConfig.shouldBeInstanceOf<GroupFlowComponent.DetailsConfig.Settings>()
            detailsConfig.isUserRequested shouldBe true
          }
        }
      }

      When("clicking on an expense") {
        val expense = FakeExpenseFactory.create(id = "exp-1")

        beforeEach {
          flowComponent.setMode(ChildPanelsMode.DUAL)
          val mainChild = flowComponent.panels.value.main.instance
          mainChild.onExpenseClicked(expense)
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("shows ExpenseDetails in details panel") {
          val detailsConfig = flowComponent.panels.value.details?.configuration
          detailsConfig.shouldNotBeNull()
          detailsConfig.shouldBeInstanceOf<GroupFlowComponent.DetailsConfig.Expense>()
          detailsConfig.expenseId shouldBe "exp-1"
        }

        And("switching mode to SINGLE") {
          beforeEach {
            flowComponent.setMode(ChildPanelsMode.SINGLE)
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("ExpenseDetails remains active as the single available panel") {
            val detailsConfig = flowComponent.panels.value.details?.configuration
            detailsConfig.shouldNotBeNull()
            detailsConfig.shouldBeInstanceOf<GroupFlowComponent.DetailsConfig.Expense>()
            detailsConfig.expenseId shouldBe "exp-1"
          }
        }
      }
    }

    Given("offline expense creation in a group") {
      val expenseRepo by testValue { koin.get<ExpenseRepository>() }
      val groupRepo by testValue { koin.get<GroupRepository>() }
      val groupDao by testValue { koin.get<GroupDao>() }

      When("creating an expense locally while offline") {
        val initialGroup =
            FakeGroupDtoFactory.create(
                id = "group-offline-1",
                name = "Offline Trip",
                lastInteractionAt = Instant.DISTANT_PAST,
            )

        beforeEach {
          groupDao.insertGroupWithMembers(
              initialGroup.let {
                GroupEntity(
                    id = it.id,
                    name = it.name,
                    inviteLink = it.inviteLink,
                    isOwner = it.isOwner,
                    lastInteractionAtEpochMillis = 0L,
                )
              },
              emptyList(),
          )

          expenseRepo.createExpense(
              groupId = "group-offline-1",
              title = "Offline Snack",
              amount = 15.0,
              creator = "user-1",
              shares = emptyList(),
              splitMethod = SplitMethod.Equally(emptyList()),
          )
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("the group's lastInteractionAt is updated optimistically in local database") {
          val group = groupRepo.getGroup("group-offline-1")
          group.shouldNotBeNull()
          group.lastInteractionAt shouldBeGreaterThan Instant.DISTANT_PAST
        }
      }
    }
  }
}
