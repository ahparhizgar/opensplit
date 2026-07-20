package com.opensplit

import com.opensplit.component.defaultCContext
import com.opensplit.component.fakeStack
import com.opensplit.dto.expense.SplitType
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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

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

        suspend fun waitForInit() {
          while (
              addExpenseComponent.uiState.value.isLoading ||
                  addExpenseComponent.uiState.value.participants.isEmpty()
          ) {
            delay(10.milliseconds)
          }
        }

        When("initial state") {
          Then("fields are empty") {
            addExpenseComponent.uiState.value.title shouldBe ""
            addExpenseComponent.uiState.value.amount shouldBe ""
            addExpenseComponent.uiState.value.fieldErrors should beEmpty()
          }
        }

        When("submitting with empty form") {
          beforeEach {
            waitForInit()
            addExpenseComponent.onSaveClicked().join()
          }
          Then("shows validation errors") {
            addExpenseComponent.uiState.value.fieldErrors["title"].shouldNotBeNull()
            addExpenseComponent.uiState.value.fieldErrors["amount"].shouldNotBeNull()
          }
        }

        When("submitting with valid form and equal split") {
          beforeEach {
            waitForInit()
            addExpenseComponent.onTitleChanged("Pizza")
            addExpenseComponent.onAmountChanged("20.0")
            addExpenseComponent.onSaveClicked().join()
          }
          Then("calls onFinished") { onFinishedCalled shouldBe true }
        }

        When("changing to percentage split") {
          beforeEach {
            waitForInit()
            addExpenseComponent.onAmountChanged("100.0")
            addExpenseComponent.onSplitTypeChanged(SplitType.PERCENTAGE)
            val members = addExpenseComponent.uiState.value.participants
            if (members.isNotEmpty()) {
              addExpenseComponent.onParticipantPercentageChanged(members[0].userId, "60")
              addExpenseComponent.onParticipantPercentageChanged(members[1].userId, "40")
            }
          }
          Then("owed amounts are calculated correctly") {
            val members = addExpenseComponent.uiState.value.participants
            if (members.size >= 2) {
              members[0].owedAmount shouldBe "60.0"
              members[1].owedAmount shouldBe "40.0"
            }
          }
        }
      }
    })
