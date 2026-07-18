package com.opensplit

import com.opensplit.component.defaultCContext
import com.opensplit.component.fakeStack
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.createComponentContext
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class AddExpenseComponentTest :
    BehaviorSpec({
      extensions(MainDispatcherExtension())
      val koin by integrationKoin()

      Given("a HouseholdDetailsComponent") {
        val cContext by testValue { defaultCContext(createComponentContext()) }
        val detailsComponent by testValue {
          cContext.navigation.navigate(
              { listOf(HouseholdDetailsComponent.Config("h1")) },
              { _, _ -> },
          )
          koin
              .get<HouseholdDetailsComponent.Factory>()
              .create(
                  cContext,
                  HouseholdDetailsComponent.Config("h1"),
              )
        }

        When("clicking on Add Expense button") {
          beforeEach { detailsComponent.onAddExpenseClicked() }
          Then("navigates to AddExpense screen") {
            cContext.fakeStack() shouldContain AddExpenseComponent.Config("h1")
          }
        }
      }

      Given("an AddExpenseComponent") {
        var onFinishedCalled by testValue { false }
        val addExpenseComponent by testValue {
          koin
              .get<AddExpenseComponent.Factory>()
              .create(
                  defaultCContext(createComponentContext()),
                  AddExpenseComponent.Config("h1"),
                  onFinished = { onFinishedCalled = true },
              )
        }

        When("initial state") {
          Then("fields are empty") {
            addExpenseComponent.uiState.value.title shouldBe ""
            addExpenseComponent.uiState.value.amount shouldBe ""
            addExpenseComponent.uiState.value.fieldErrors should beEmpty()
          }
        }

        When("submitting with empty form") {
          beforeEach { addExpenseComponent.onSaveClicked().join() }
          Then("shows validation errors") {
            addExpenseComponent.uiState.value.fieldErrors["title"].shouldNotBeNull()
            addExpenseComponent.uiState.value.fieldErrors["amount"].shouldNotBeNull()
          }
        }

        When("submitting with valid form") {
          beforeEach {
            addExpenseComponent.onTitleChanged("Pizza")
            addExpenseComponent.onAmountChanged("25.0")
            addExpenseComponent.onSaveClicked().join()
          }
          Then("calls onFinished") { onFinishedCalled shouldBe true }
        }
      }
    })
