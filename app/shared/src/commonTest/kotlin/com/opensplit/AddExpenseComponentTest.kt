package com.opensplit

import com.opensplit.component.TestCContext
import com.opensplit.db.ExpenseDao
import com.opensplit.db.SyncQueueDao
import com.opensplit.domain.ParticipantShare
import com.opensplit.dto.auth.UserProfile
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.PayAmountsUiState
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.HouseholdRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class AddExpenseComponentTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("an AddExpenseComponent") {
      var onFinishedCalled by testValue { false }
      val addExpenseComponent by testValue {
        koin
            .get<AddExpenseComponent.Factory>()
            .create(
                TestCContext(),
                AddExpenseComponent.Config("h1"),
                onFinished = { onFinishedCalled = true },
            )
      }

      When("initial state") {
        Then("fields are empty") {
          addExpenseComponent.uiState.value.title shouldBe ""
          (addExpenseComponent.uiState.value.payAmounts as PayAmountsUiState.OnePerson)
              .amount shouldBe ""
          addExpenseComponent.uiState.value.fieldErrors should beEmpty()
        }
      }

      When("submitting with empty form") {
        beforeEach {
          testCoroutineScheduler.advanceUntilIdle()
          addExpenseComponent.onSaveClicked().join()
        }
        Then("shows validation errors") {
          addExpenseComponent.uiState.value.fieldErrors["title"].shouldNotBeNull()
          addExpenseComponent.uiState.value.fieldErrors["amount"].shouldNotBeNull()
        }
        And("typing a character in fields") {
          beforeEach {
            testCoroutineScheduler.advanceUntilIdle()
            addExpenseComponent.onAmountChanged("1")
            addExpenseComponent.onTitleChanged("T")
          }
          Then("field errors are cleared") {
            addExpenseComponent.uiState.value.fieldErrors["title"] shouldBe null
            addExpenseComponent.uiState.value.fieldErrors["amount"] shouldBe null
          }
          And("clearing them doesn't show the error again") {
            beforeEach {
              testCoroutineScheduler.advanceUntilIdle()
              addExpenseComponent.onTitleChanged("")
              addExpenseComponent.onAmountChanged("")
            }
            Then("title error is still cleared") {
              addExpenseComponent.uiState.value.fieldErrors["title"] shouldBe null
              addExpenseComponent.uiState.value.fieldErrors["amount"] shouldBe null
            }
          }
        }
      }

      When("submitting with valid form and equal split") {
        beforeEach {
          testCoroutineScheduler.advanceUntilIdle()
          addExpenseComponent.onTitleChanged("Pizza")
          addExpenseComponent.onAmountChanged("20.0")
          addExpenseComponent.onSaveClicked().join()
        }
        Then("it's added to DB") {
          onFinishedCalled shouldBe true
          koin.get<ExpenseDao>().getExpenses(householdId = "h1").first().let {
            it shouldHaveSize 1
            it.first().title shouldBe "Pizza"
          }
          koin.get<SyncQueueDao>().getQueue().first().let {
            it shouldHaveSize 1
            it.first().entityType shouldBe "EXPENSE"
          }
        }
      }
    }

    Given("an AddExpenseComponent in Edit Mode") {
      val householdId = "household-1"
      val u1 = "user-1"
      val u2 = "user-2"

      val profileRepository by testValue { koin.get<ProfileRepository>() }
      val householdRepository by testValue { koin.get<HouseholdRepository>() }
      val expenseRepository by testValue { koin.get<ExpenseRepository>() }

      beforeEach {
        profileRepository.setProfile(UserProfile(u1, "Amir", "amir@example.com"))
        householdRepository.refresh()
        testCoroutineScheduler.advanceUntilIdle()
      }

      When("Restoring 'Percentage' split method") {
        val method = SplitMethod.Percentage(mapOf(u1 to 70.0, u2 to 30.0))
        beforeEach {
          expenseRepository.createExpense(
              householdId = householdId,
              title = "Dinner",
              amount = 100.0,
              payerId = u1,
              shares =
                  listOf(
                      ParticipantShare(u1, 100.0, 70.0),
                      ParticipantShare(u2, 0.0, 30.0),
                  ),
              splitMethod = method,
          )
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("it restores the percentage method and values") {
          val createdExpense = koin.get<ExpenseDao>().getExpenses(householdId).first().first()
          val component =
              koin
                  .get<AddExpenseComponent.Factory>()
                  .create(
                      TestCContext(),
                      AddExpenseComponent.Config(householdId, createdExpense.id),
                      onFinished = {},
                  )
          testCoroutineScheduler.advanceUntilIdle()

          component.uiState.value.splitMethod shouldBe method
          component.uiState.value.title shouldBe "Dinner"
          component.uiState.value.amountSum shouldBe 100.0
        }
      }

      When("Restoring 'Shares' split method") {
        val method = SplitMethod.Shares(mapOf(u1 to 2, u2 to 1))
        beforeEach {
          expenseRepository.createExpense(
              householdId = householdId,
              title = "Lunch",
              amount = 90.0,
              payerId = u1,
              shares =
                  listOf(
                      ParticipantShare(u1, 90.0, 60.0),
                      ParticipantShare(u2, 0.0, 30.0),
                  ),
              splitMethod = method,
          )
          testCoroutineScheduler.advanceUntilIdle()
        }
        Then("it restores the shares method") {
          val createdExpense = koin.get<ExpenseDao>().getExpenses(householdId).first().last()
          val component =
              koin
                  .get<AddExpenseComponent.Factory>()
                  .create(
                      TestCContext(),
                      AddExpenseComponent.Config(householdId, createdExpense.id),
                      onFinished = {},
                  )
          testCoroutineScheduler.advanceUntilIdle()
          component.uiState.value.splitMethod shouldBe method
        }
      }

      When("Restoring 'Adjustment' split method") {
        val method = SplitMethod.Adjustment(mapOf(u1 to 10.0))
        beforeEach {
          expenseRepository.createExpense(
              householdId = householdId,
              title = "Taxi",
              amount = 50.0,
              payerId = u1,
              shares =
                  listOf(
                      ParticipantShare(u1, 50.0, 30.0),
                      ParticipantShare(u2, 0.0, 20.0),
                  ),
              splitMethod = method,
          )
          testCoroutineScheduler.advanceUntilIdle()
        }
        Then("it restores the adjustment method") {
          val createdExpense = koin.get<ExpenseDao>().getExpenses(householdId).first().last()
          val component =
              koin
                  .get<AddExpenseComponent.Factory>()
                  .create(
                      TestCContext(),
                      AddExpenseComponent.Config(householdId, createdExpense.id),
                      onFinished = {},
                  )
          testCoroutineScheduler.advanceUntilIdle()
          component.uiState.value.splitMethod shouldBe method
        }
      }

      When("Auto-recalculate percentages when total amount changes") {
        val method = SplitMethod.Percentage(mapOf(u1 to 50.0, u2 to 50.0))
        beforeEach {
          expenseRepository.createExpense(
              householdId = householdId,
              title = "Split",
              amount = 100.0,
              payerId = u1,
              shares =
                  listOf(
                      ParticipantShare(u1, 100.0, 50.0),
                      ParticipantShare(u2, 0.0, 50.0),
                  ),
              splitMethod = method,
          )
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("recalculates amounts correctly") {
          val createdExpense = koin.get<ExpenseDao>().getExpenses(householdId).first().last()
          val component =
              koin
                  .get<AddExpenseComponent.Factory>()
                  .create(
                      TestCContext(),
                      AddExpenseComponent.Config(householdId, createdExpense.id),
                      onFinished = {},
                  )
          testCoroutineScheduler.advanceUntilIdle()

          component.onAmountChanged("200.0")

          val calculated =
              component.uiState.value.splitMethod.calculateConsumedAmounts(
                  totalAmount = 200.0,
                  allMembers = setOf(u1, u2),
              )
          calculated.find { it.userId == u1 }?.amount shouldBe 100.0
          calculated.find { it.userId == u2 }?.amount shouldBe 100.0
        }
      }

      When("Preserving fixed 'Unequally' amounts when total amount changes") {
        val method = SplitMethod.Unequally(mapOf(u1 to 40.0, u2 to 60.0))
        beforeEach {
          expenseRepository.createExpense(
              householdId = householdId,
              title = "Fixed Split",
              amount = 100.0,
              payerId = u1,
              shares =
                  listOf(
                      ParticipantShare(u1, 100.0, 40.0),
                      ParticipantShare(u2, 0.0, 60.0),
                  ),
              splitMethod = method,
          )
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("keeps the fixed amounts and shows validation error") {
          val createdExpense = koin.get<ExpenseDao>().getExpenses(householdId).first().last()
          val component =
              koin
                  .get<AddExpenseComponent.Factory>()
                  .create(
                      TestCContext(),
                      AddExpenseComponent.Config(householdId, createdExpense.id),
                      onFinished = {},
                  )
          testCoroutineScheduler.advanceUntilIdle()

          component.onAmountChanged("120.0")

          component.uiState.value.splitMethod shouldBe method // Still fixed to 40/60

          component.onSaveClicked().join()
          component.uiState.value.fieldErrors["amount"].shouldNotBeNull()
        }
      }

      When("Switch split method during edit") {
        val component by testValue {
          koin
              .get<AddExpenseComponent.Factory>()
              .create(
                  TestCContext(),
                  AddExpenseComponent.Config(householdId),
                  onFinished = {},
              )
        }
        beforeEach {
          testCoroutineScheduler.advanceUntilIdle()
          component.onAmountChanged("100.0")
        }

        Then("switches method correctly and keeps fields empty") {
          component.navigateToAdjustSplit()
          val moreOptions =
              (component.stack.value.active.instance as AddExpenseComponent.Child.MoreSplitOptions)
                  .component

          moreOptions.onTabChanged(com.opensplit.dto.expense.SplitType.PERCENTAGE)
          val percentageState = moreOptions.percentageComponent.uiState.value
          percentageState.percentages should beEmpty()
        }
      }
    }
  }
}
