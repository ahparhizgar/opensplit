package com.opensplit

import com.opensplit.component.TestCContext
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.PayAmountsUiState
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

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
      }

      When("submitting with valid form and equal split") {
        beforeEach {
          testCoroutineScheduler.advanceUntilIdle()
          addExpenseComponent.onTitleChanged("Pizza")
          addExpenseComponent.onAmountChanged("20.0")
          addExpenseComponent.onSaveClicked().join()
        }
        Then("calls onFinished") { onFinishedCalled shouldBe true }
      }
    }
  }
}
