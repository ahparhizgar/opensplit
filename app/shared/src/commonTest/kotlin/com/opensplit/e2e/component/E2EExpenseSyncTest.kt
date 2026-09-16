package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.dto.auth.UserProfile
import com.opensplit.dto.household.FakeHouseholdDtoFactory
import com.opensplit.fake.FakeExpenseApi
import com.opensplit.fake.FakeHouseholdApi
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.repository.HouseholdRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class E2EExpenseSyncTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a household details screen") {
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      val fakeHouseholdApi by testValue { koin.get<FakeHouseholdApi>() }
      val householdRepo by testValue { koin.get<HouseholdRepository>() }
      val fakeExpenseApi by testValue { koin.get<FakeExpenseApi>() }

      val householdDetailsComponent by testValue {
        koin
            .get<HouseholdDetailsComponent.Factory>()
            .create(TestCContext().resumed(), HouseholdDetailsComponent.Config("household-1"))
      }

      beforeEach {
        profileRepo.setProfile(UserProfile("user-1", "User 1", "user-1@example.com"))
        fakeHouseholdApi.households =
            listOf(FakeHouseholdDtoFactory.create(id = "household-1", name = "Test House"))
        householdRepo.refresh()
        testCoroutineScheduler.advanceUntilIdle()
      }

      Then("initial state has no expenses") {
        householdDetailsComponent.uiState.value.expenses.shouldBeEmpty()
      }

      When("adding an expense via AddExpenseComponent") {
        var addFinished = false
        val addExpenseComponent by testValue {
          koin
              .get<AddExpenseComponent.Factory>()
              .create(
                  TestCContext().resumed(),
                  AddExpenseComponent.Config("household-1"),
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
          val expenses = householdDetailsComponent.uiState.value.expenses
          expenses shouldHaveSize 1
          expenses.first().title shouldBe "Pizza"
          expenses.first().amount shouldBe 30.0
        }

        And("sync daemon processes outbox") {
          beforeEach { testCoroutineScheduler.advanceUntilIdle() }

          Then("proper API call is executed and UI is still correct") {
            fakeExpenseApi.createdExpenses shouldHaveSize 1
            fakeExpenseApi.createdExpenses.first().let {
              it.householdId shouldBe "household-1"
              it.title shouldBe "Pizza"
              it.amount shouldBe 30.0
            }

            val expenses = householdDetailsComponent.uiState.value.expenses
            expenses shouldHaveSize 1
            expenses.first().title shouldBe "Pizza"
            expenses.first().amount shouldBe 30.0
          }

          And("editing the expense via AddExpenseComponent") {
            var editFinished = false

            beforeEach {
              editFinished = false
              val targetExpenseId =
                  householdDetailsComponent.uiState.value.expenses.first().id
              val editExpenseComponent =
                  koin
                      .get<AddExpenseComponent.Factory>()
                      .create(
                          TestCContext().resumed(),
                          AddExpenseComponent.Config("household-1", targetExpenseId),
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
              val expenses = householdDetailsComponent.uiState.value.expenses
              expenses shouldHaveSize 1
              expenses.first().title shouldBe "Fancy Pizza"
              expenses.first().amount shouldBe 45.0
            }

            And("sync daemon processes update") {
              beforeEach { testCoroutineScheduler.advanceUntilIdle() }

              Then("UI remains correct after sync") {
                val expenses = householdDetailsComponent.uiState.value.expenses
                expenses shouldHaveSize 1
                expenses.first().title shouldBe "Fancy Pizza"
                expenses.first().amount shouldBe 45.0
              }

              And("deleting the expense via ExpenseDetailsComponent") {
                var backCalled = false
                var deleteTargetId = ""

                beforeEach {
                  backCalled = false
                  deleteTargetId =
                      householdDetailsComponent.uiState.value.expenses.first().id
                  val expenseDetailsComponent =
                      koin
                          .get<ExpenseDetailsComponent.Factory>()
                          .create(
                              TestCContext().resumed(),
                              ExpenseDetailsComponent.Config("household-1", deleteTargetId),
                              onBack = { backCalled = true },
                          )

                  testCoroutineScheduler.advanceUntilIdle()
                  expenseDetailsComponent.onDeleteClicked()
                  testCoroutineScheduler.advanceUntilIdle()
                }

                Then("optimistic UI shows expense deleted immediately") {
                  backCalled shouldBe true
                  householdDetailsComponent.uiState.value.expenses.shouldBeEmpty()
                }

                And("sync daemon processes deletion") {
                  beforeEach { testCoroutineScheduler.advanceUntilIdle() }

                  Then("delete API call is executed and UI remains empty") {
                    fakeExpenseApi.deletedCalls shouldHaveSize 1
                    fakeExpenseApi.deletedCalls.first() shouldBe
                        Pair("household-1", deleteTargetId)
                    householdDetailsComponent.uiState.value.expenses.shouldBeEmpty()
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
