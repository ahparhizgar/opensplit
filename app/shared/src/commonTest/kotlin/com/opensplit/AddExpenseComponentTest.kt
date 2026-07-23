package com.opensplit

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.opensplit.component.defaultCContext
import com.opensplit.component.fakeStack
import com.opensplit.dto.household.FakeHouseholdDtoFactory
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.PayAmountsUiState
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.createComponentContext
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class AddExpenseComponentTest :
    BehaviorSpec({
      extensions(MainDispatcherExtension())
      val koin by integrationKoin()

      Given("a HouseholdDetailsComponent") {
        val cContext by testValue {
          defaultCContext(
              createComponentContext(lifecycle = LifecycleRegistry(Lifecycle.State.RESUMED))
          )
        }
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
          beforeEach {
            testCoroutineScheduler.advanceUntilIdle()
            detailsComponent.onAddExpenseClicked()
          }
          Then("navigates to AddExpense screen") {
            cContext.fakeStack().last().shouldBeInstanceOf<AddExpenseComponent.Config>()
          }
        }
      }

      Given("an AddExpenseComponent") {
        var onFinishedCalled by testValue { false }
        val household = FakeHouseholdDtoFactory.create(id = "h1")
        val me = household.members.first { it.isCurrentUser }
        val addExpenseComponent by testValue {
          koin
              .get<AddExpenseComponent.Factory>()
              .create(
                  defaultCContext(createComponentContext()),
                  AddExpenseComponent.Config("h1", household, me),
                  household,
                  me,
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
    })
