package com.opensplit.integration.component

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.opensplit.component.TestCContext
import com.opensplit.component.fakeStack
import com.opensplit.dto.expense.SplitMethod.Equally
import com.opensplit.integration.expense.AddExpenseFlowComponent
import com.opensplit.integration.expense.ExpenseDetailsComponent
import com.opensplit.integration.expense.ExpenseDetailsComponentFactory
import com.opensplit.integration.group.details.GroupDetailsComponent
import com.opensplit.integration.group.details.GroupDetailsComponentFactory
import com.opensplit.repository.ExpenseRepository
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

@OptIn(ExperimentalDecomposeApi::class)
class ExpenseDetailsComponentTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a GroupDetailsComponent") {
      val cContext by testValue { TestCContext().resumed() }
      val detailsComponent by testValue {
        koin
            .get<GroupDetailsComponentFactory>()
            .create(
                cContext,
                GroupDetailsComponent.Config("group-1"),
            )
      }
      beforeEach {
        koin
            .get<ExpenseRepository>()
            .createExpense(
                groupId = "group-1",
                title = "Pizza",
                amount = 20.0,
                creator = "user-1",
                shares = emptyList(),
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

    Given("an ExpenseDetailsComponent") {
      val cContext by testValue { TestCContext().resumed() }
      val expenseDetailsComponent by testValue {
        koin
            .get<ExpenseDetailsComponentFactory>()
            .create(
                cContext,
                ExpenseDetailsComponent.Config("group-1", "expense-1"),
                onBack = {},
            )
      }

      When("onEditClicked is called") {
        beforeEach { expenseDetailsComponent.onEditClicked() }

        Then("navigates to AddExpense flow in edit mode") {
          val lastConfig = cContext.fakeStack().last()
          lastConfig.shouldBeInstanceOf<AddExpenseFlowComponent.Config>()
          lastConfig.expenseId shouldBe "expense-1"
          lastConfig.groupId shouldBe "group-1"
        }
      }
    }
  }
}
