package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.dto.auth.UserProfile
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.group.FakeGroupDtoFactory
import com.opensplit.fake.FakeExpenseApi
import com.opensplit.fake.FakeGroupApi
import com.opensplit.fake.FakeSyncApi
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.AddExpenseComponentFactory
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.expense.ExpenseDetailsComponentFactory
import com.opensplit.features.group.details.GroupDetailsComponent
import com.opensplit.features.group.details.GroupDetailsComponentFactory
import com.opensplit.repository.GroupRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.sync.SyncManager
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.time.Clock

class E2EExpenseSyncTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a group details screen for client to server sync") {
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      val fakeGroupApi by testValue { koin.get<FakeGroupApi>() }
      val groupRepo by testValue { koin.get<GroupRepository>() }
      val fakeExpenseApi by testValue { koin.get<FakeExpenseApi>() }

      val groupDetailsComponent by testValue {
        koin
            .get<GroupDetailsComponentFactory>()
            .create(TestCContext().resumed(), GroupDetailsComponent.Config("group-1"))
      }

      beforeEach {
        profileRepo.setProfile(UserProfile("user-1", "User 1", "user-1@example.com"))
        fakeGroupApi.groups =
            listOf(FakeGroupDtoFactory.create(id = "group-1", name = "Test House"))
        groupRepo.refresh()
        testCoroutineScheduler.advanceUntilIdle()
      }

      Then("initial state has no expenses") {
        groupDetailsComponent.uiState.value.expenses.shouldBeEmpty()
      }

      When("adding an expense via AddExpenseComponent") {
        var addFinished = false
        val addExpenseComponent by testValue {
          koin
              .get<AddExpenseComponentFactory>()
              .create(
                  TestCContext().resumed(),
                  AddExpenseComponent.Config("group-1"),
                  onFinished = { addFinished = true },
              )
        }

        beforeEach {
          addFinished = false
          addExpenseComponent.onTitleChanged("Pizza")
          addExpenseComponent.onAmountChanged("30.0")
          addExpenseComponent.onSaveClicked().join()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("optimistic UI shows updated state immediately") {
          addFinished shouldBe true
          val expenses = groupDetailsComponent.uiState.value.expenses
          expenses shouldHaveSize 1
          expenses.first().title shouldBe "Pizza"
          expenses.first().amount shouldBe 30.0
        }

        And("sync daemon processes outbox") {
          beforeEach { testCoroutineScheduler.advanceUntilIdle() }

          Then("proper API call is executed and UI is still correct") {
            fakeExpenseApi.createdExpenses shouldHaveSize 1
            fakeExpenseApi.createdExpenses.first().let {
              it.groupId shouldBe "group-1"
              it.title shouldBe "Pizza"
              it.amount shouldBe 30.0
            }

            val expenses = groupDetailsComponent.uiState.value.expenses
            expenses shouldHaveSize 1
            expenses.first().title shouldBe "Pizza"
            expenses.first().amount shouldBe 30.0
          }

          And("editing the expense via AddExpenseComponent") {
            var editFinished = false

            beforeEach {
              editFinished = false
              val targetExpenseId = groupDetailsComponent.uiState.value.expenses.first().id
              val editExpenseComponent =
                  koin
                      .get<AddExpenseComponentFactory>()
                      .create(
                          TestCContext().resumed(),
                          AddExpenseComponent.Config("group-1", targetExpenseId),
                          onFinished = { editFinished = true },
                      )

              testCoroutineScheduler.advanceUntilIdle()
              editExpenseComponent.onTitleChanged("Fancy Pizza")
              editExpenseComponent.onAmountChanged("45.0")
              editExpenseComponent.onSaveClicked().join()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("optimistic UI shows edited state immediately") {
              editFinished shouldBe true
              val expenses = groupDetailsComponent.uiState.value.expenses
              expenses shouldHaveSize 1
              expenses.first().title shouldBe "Fancy Pizza"
              expenses.first().amount shouldBe 45.0
            }

            And("sync daemon processes update") {
              beforeEach { testCoroutineScheduler.advanceUntilIdle() }

              Then("UI remains correct after sync") {
                val expenses = groupDetailsComponent.uiState.value.expenses
                expenses shouldHaveSize 1
                expenses.first().title shouldBe "Fancy Pizza"
                expenses.first().amount shouldBe 45.0
              }

              And("deleting the expense via ExpenseDetailsComponent") {
                var backCalled = false
                var deleteTargetId = ""

                beforeEach {
                  backCalled = false
                  deleteTargetId = groupDetailsComponent.uiState.value.expenses.first().id
                  val expenseDetailsComponent =
                      koin
                          .get<ExpenseDetailsComponentFactory>()
                          .create(
                              TestCContext().resumed(),
                              ExpenseDetailsComponent.Config("group-1", deleteTargetId),
                              onBack = { backCalled = true },
                          )

                  testCoroutineScheduler.advanceUntilIdle()
                  expenseDetailsComponent.onDeleteClicked()
                  testCoroutineScheduler.advanceUntilIdle()
                }

                Then("optimistic UI shows expense deleted immediately") {
                  backCalled shouldBe true
                  groupDetailsComponent.uiState.value.expenses.shouldBeEmpty()
                }

                And("sync daemon processes deletion") {
                  beforeEach { testCoroutineScheduler.advanceUntilIdle() }

                  Then("delete API call is executed and UI remains empty") {
                    fakeExpenseApi.deletedCalls shouldHaveSize 1
                    fakeExpenseApi.deletedCalls.first() shouldBe Pair("group-1", deleteTargetId)
                    groupDetailsComponent.uiState.value.expenses.shouldBeEmpty()
                  }
                }
              }
            }
          }
        }
      }
    }

    Given("a group details screen for server to client sync") {
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      val fakeGroupApi by testValue { koin.get<FakeGroupApi>() }
      val groupRepo by testValue { koin.get<GroupRepository>() }
      val fakeSyncApi by testValue { koin.get<FakeSyncApi>() }
      val syncManager by testValue { koin.get<SyncManager>() }

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

      Then("initial state has no expenses") {
        groupDetailsComponent.uiState.value.expenses.shouldBeEmpty()
      }

      When("a new expense is created on the server by another member") {
        val serverExpense =
            ExpenseDto(
                id = "server-expense-1",
                groupId = "group-1",
                title = "Sushi Dinner",
                amount = 60.0,
                creator = "user-2",
                createdAt = Clock.System.now(),
                shares =
                    listOf(
                        ParticipantShareDto("user-1", 0.0, 30.0),
                        ParticipantShareDto("user-2", 60.0, 30.0),
                    ),
                splitMethod = SplitMethod.Equally(listOf("user-1", "user-2")),
                syncStatus = SyncStatus.SYNCED,
            )

        beforeEach {
          fakeSyncApi.expenses = listOf(serverExpense)
          syncManager.sync()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("UI state is updated with the new expense from the server") {
          val expenses = groupDetailsComponent.uiState.value.expenses
          expenses shouldHaveSize 1
          expenses.first().let {
            it.id shouldBe "server-expense-1"
            it.title shouldBe "Sushi Dinner"
            it.amount shouldBe 60.0
            it.creator shouldBe "user-2"
          }
        }

        And("member balances are updated according to server shares") {
          Then("user-1 owes 30.0 and user-2 is owed 30.0") {
            val members = groupDetailsComponent.uiState.value.group?.members
            members?.first { it.userId == "user-1" }?.balance shouldBe -30.0
            members?.first { it.userId == "user-2" }?.balance shouldBe 30.0
          }
        }

        And("the server expense is updated with new title and amount") {
          val updatedServerExpense =
              serverExpense.copy(
                  title = "Fancy Sushi Dinner",
                  amount = 100.0,
                  shares =
                      listOf(
                          ParticipantShareDto("user-1", 0.0, 50.0),
                          ParticipantShareDto("user-2", 100.0, 50.0),
                      ),
              )

          beforeEach {
            fakeSyncApi.expenses = listOf(updatedServerExpense)
            syncManager.sync()
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("UI state reflects the updated title and amount") {
            val expenses = groupDetailsComponent.uiState.value.expenses
            expenses shouldHaveSize 1
            expenses.first().let {
              it.id shouldBe "server-expense-1"
              it.title shouldBe "Fancy Sushi Dinner"
              it.amount shouldBe 100.0
            }
          }

          And("member balances reflect the updated shares") {
            Then("user-1 owes 50.0 and user-2 is owed 50.0") {
              val members = groupDetailsComponent.uiState.value.group?.members
              members?.first { it.userId == "user-1" }?.balance shouldBe -50.0
              members?.first { it.userId == "user-2" }?.balance shouldBe 50.0
            }
          }

          And("the expense is deleted on the server") {
            beforeEach {
              fakeSyncApi.expenses = emptyList()
              fakeSyncApi.deletedExpenseIds = listOf("server-expense-1")
              syncManager.sync()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("UI state is empty and expense is removed") {
              groupDetailsComponent.uiState.value.expenses.shouldBeEmpty()
            }

            And("member balances revert back to zero") {
              Then("balances are reset to 0.0") {
                val members = groupDetailsComponent.uiState.value.group?.members
                members?.first { it.userId == "user-1" }?.balance shouldBe 0.0
                members?.first { it.userId == "user-2" }?.balance shouldBe 0.0
              }
            }
          }
        }
      }
    }
  }
}
