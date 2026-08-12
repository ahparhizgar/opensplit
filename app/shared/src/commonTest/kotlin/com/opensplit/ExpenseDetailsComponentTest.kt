package com.opensplit

import com.opensplit.component.TestCContext
import com.opensplit.component.fakeStack
import com.opensplit.dto.expense.SplitMethod.Equally
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.repository.ExpenseRepository
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf

class ExpenseDetailsComponentTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a HouseholdDetailsComponent") {
      val cContext by testValue { TestCContext().resumed() }
      val detailsComponent by testValue {
        koin
            .get<HouseholdDetailsComponent.Factory>()
            .create(
                cContext,
                HouseholdDetailsComponent.Config("household-1"),
            )
      }
      beforeEach {
        koin
            .get<ExpenseRepository>()
            .createExpense(
                householdId = "household-1",
                title = "Pizza",
                amount = 20.0,
                payerId = "user-1",
                participants = emptyList(),
                splitMethod = Equally(emptyList()),
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
}
