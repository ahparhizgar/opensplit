package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.dto.auth.UserProfile
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.group.FakeGroupDtoFactory
import com.opensplit.fake.FakeGroupApi
import com.opensplit.fake.FakeSyncApi
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.AddExpenseComponentFactory
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.expense.ExpenseDetailsComponentFactory
import com.opensplit.features.group.details.GroupDetailsComponent
import com.opensplit.features.group.details.GroupDetailsComponentFactory
import com.opensplit.features.group.my.MyGroupsListComponentFactory
import com.opensplit.repository.GroupRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.sync.SyncManager
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.shouldBe
import kotlin.time.Clock

class E2EGroupBalanceTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a client app with group list and details components") {
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      val fakeGroupApi by testValue { koin.get<FakeGroupApi>() }
      val groupRepo by testValue { koin.get<GroupRepository>() }
      val fakeSyncApi by testValue { koin.get<FakeSyncApi>() }
      val syncManager by testValue { koin.get<SyncManager>() }

      val myGroupsListComponent by testValue {
        koin.get<MyGroupsListComponentFactory>().create(TestCContext().resumed())
      }

      val groupDetailsComponent by testValue {
        koin
            .get<GroupDetailsComponentFactory>()
            .create(TestCContext().resumed(), GroupDetailsComponent.Config("group-1"))
      }

      beforeEach {
        profileRepo.setProfile(UserProfile("user-1", "User 1", "user-1@example.com"))
        fakeGroupApi.groups =
            listOf(FakeGroupDtoFactory.create(id = "group-1", name = "Test House"))
        fakeSyncApi.expenses = emptyList()
        fakeSyncApi.deletedExpenseIds = emptyList()
        groupRepo.refresh()
        testCoroutineScheduler.advanceUntilIdle()
      }

      Then("initial group balance is zero in both components") {
        myGroupsListComponent.uiState.value.groups.first { it.id == "group-1" }.balance shouldBe 0.0
        groupDetailsComponent.uiState.value.group?.balance shouldBe 0.0
      }

      When("adding an expense locally") {
        beforeEach {
          val addExpenseComponent =
              koin
                  .get<AddExpenseComponentFactory>()
                  .create(
                      TestCContext().resumed(),
                      AddExpenseComponent.Config("group-1"),
                      onFinished = {},
                  )
          testCoroutineScheduler.advanceUntilIdle()
          addExpenseComponent.onTitleChanged("Dinner")
          addExpenseComponent.onAmountChanged("60.0")
          addExpenseComponent.onSaveClicked().join()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("group balance is updated locally after add") {
          myGroupsListComponent.uiState.value.groups.first { it.id == "group-1" }.balance shouldBe
              30.0
          groupDetailsComponent.uiState.value.group?.balance shouldBe 30.0
        }

        And("updating the expense locally") {
          beforeEach {
            val targetExpenseId = groupDetailsComponent.uiState.value.expenses.first().id
            val editExpenseComponent =
                koin
                    .get<AddExpenseComponentFactory>()
                    .create(
                        TestCContext().resumed(),
                        AddExpenseComponent.Config("group-1", targetExpenseId),
                        onFinished = {},
                    )
            testCoroutineScheduler.advanceUntilIdle()
            editExpenseComponent.onTitleChanged("Fancy Dinner")
            editExpenseComponent.onAmountChanged("100.0")
            editExpenseComponent.onSaveClicked().join()
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("group balance is updated locally after update") {
            myGroupsListComponent.uiState.value.groups.first { it.id == "group-1" }.balance shouldBe
                50.0
            groupDetailsComponent.uiState.value.group?.balance shouldBe 50.0
          }

          And("deleting the expense locally") {
            beforeEach {
              val targetExpenseId = groupDetailsComponent.uiState.value.expenses.first().id
              val expenseDetailsComponent =
                  koin
                      .get<ExpenseDetailsComponentFactory>()
                      .create(
                          TestCContext().resumed(),
                          ExpenseDetailsComponent.Config("group-1", targetExpenseId),
                          onBack = {},
                      )
              testCoroutineScheduler.advanceUntilIdle()
              expenseDetailsComponent.onDeleteClicked()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("group balance is updated locally after delete") {
              myGroupsListComponent.uiState.value.groups
                  .first { it.id == "group-1" }
                  .balance shouldBe 0.0
              groupDetailsComponent.uiState.value.group?.balance shouldBe 0.0
            }
          }
        }
      }

      When("adding an expense remotely via sync") {
        val serverExpense =
            ExpenseDto(
                id = "server-expense-1",
                groupId = "group-1",
                title = "Groceries",
                amount = 80.0,
                creator = "user-2",
                createdAt = Clock.System.now(),
                shares =
                    listOf(
                        ParticipantShareDto("user-1", 0.0, 40.0),
                        ParticipantShareDto("user-2", 80.0, 40.0),
                    ),
                splitMethod = SplitMethod.Equally(listOf("user-1", "user-2")),
                syncStatus = SyncStatus.SYNCED,
            )

        beforeEach {
          fakeSyncApi.expenses = listOf(serverExpense)
          syncManager.sync()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("group balance is updated remotely after add sync") {
          myGroupsListComponent.uiState.value.groups.first { it.id == "group-1" }.balance shouldBe
              -40.0
          groupDetailsComponent.uiState.value.group?.balance shouldBe -40.0
        }

        And("updating the expense remotely via sync") {
          val updatedServerExpense =
              serverExpense.copy(
                  amount = 120.0,
                  shares =
                      listOf(
                          ParticipantShareDto("user-1", 0.0, 60.0),
                          ParticipantShareDto("user-2", 120.0, 60.0),
                      ),
              )

          beforeEach {
            fakeSyncApi.expenses = listOf(updatedServerExpense)
            syncManager.sync()
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("group balance is updated remotely after update sync") {
            myGroupsListComponent.uiState.value.groups.first { it.id == "group-1" }.balance shouldBe
                -60.0
            groupDetailsComponent.uiState.value.group?.balance shouldBe -60.0
          }

          And("deleting the expense remotely via sync") {
            beforeEach {
              fakeSyncApi.expenses = emptyList()
              fakeSyncApi.deletedExpenseIds = listOf("server-expense-1")
              syncManager.sync()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("group balance is updated remotely after delete sync") {
              myGroupsListComponent.uiState.value.groups
                  .first { it.id == "group-1" }
                  .balance shouldBe 0.0
              groupDetailsComponent.uiState.value.group?.balance shouldBe 0.0
            }
          }
        }
      }
    }
  }
}
