package com.opensplit

import com.opensplit.component.TestCContext
import com.opensplit.db.ExpenseDao
import com.opensplit.db.SyncQueueDao
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.PayAmountsUiState
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
  }
}
