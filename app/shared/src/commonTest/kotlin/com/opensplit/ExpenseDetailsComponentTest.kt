package com.opensplit

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.opensplit.component.TestCContext
import com.opensplit.component.defaultCContext
import com.opensplit.component.fakeStack
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.repository.ExpenseRepository
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.createComponentContext
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first

class ExpenseDetailsComponentTest :
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
          koin
              .get<HouseholdDetailsComponent.Factory>()
              .create(
                  cContext,
                  HouseholdDetailsComponent.Config("household-1"),
              )
        }

        When("expenses exist") {
          beforeEach {
            koin
                .get<ExpenseRepository>()
                .createExpense(
                    "household-1",
                    "Pizza",
                    20.0,
                    "user-1",
                    emptyList(),
                    com.opensplit.dto.expense.SplitMethod.Equally(emptyList()),
                )
            testCoroutineScheduler.advanceUntilIdle()
          }
          And("clicking on an expense") {
            beforeEach {
              detailsComponent.uiState.value.expenses.shouldNotBeEmpty()
              val expense = detailsComponent.uiState.value.expenses.first()
              detailsComponent.onExpenseClicked(expense)
            }
            Then("navigates to ExpenseDetails screen") {
              cContext.fakeStack().last().shouldBeInstanceOf<ExpenseDetailsComponent.Config>()
            }
          }
        }
      }

      Given("an ExpenseDetailsComponent") {
        val householdId = "household-1"

        When("an expense is created") {
          beforeEach {
            koin
                .get<ExpenseRepository>()
                .createExpense(
                    householdId,
                    "Pizza",
                    20.0,
                    "user-1",
                    emptyList(),
                    com.opensplit.dto.expense.SplitMethod.Equally(emptyList()),
                )
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("it can be loaded in the component") {
            val expense = koin.get<ExpenseRepository>().getExpenses(householdId).first().first()
            val component =
                koin
                    .get<ExpenseDetailsComponent.Factory>()
                    .create(
                        context = TestCContext(),
                        config = ExpenseDetailsComponent.Config(householdId, expense.id),
                        onBack = {},
                    )
            testCoroutineScheduler.advanceUntilIdle()
            component.uiState.value.expense.shouldNotBeNull()
            component.uiState.value.expense?.title shouldBe "Pizza"
          }
        }
      }
    })
