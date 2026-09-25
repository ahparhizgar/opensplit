package com.opensplit.integration.component

import com.arkivanov.decompose.value.MutableValue
import com.opensplit.component.TestCContext
import com.opensplit.domain.FakeGroupFactory
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.integration.expense.AddExpenseUiState
import com.opensplit.integration.expense.DefaultPaidAmountsComponent
import com.opensplit.integration.expense.PayAmountsUiState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PaidAmountsComponentTest : BehaviorSpec() {
  init {
    Given("a DefaultPaidAmountsComponent") {
      val member1 = FakeMemberFactory.create(userId = "u1", name = "User 1")
      val group = FakeGroupFactory.create(members = listOf(member1))
      val cContext = TestCContext()

      When("initialized with OnePerson and null amount") {
        val parentUiState =
            MutableValue(
                AddExpenseUiState(
                    groupName = "Group",
                    title = "Title",
                    allParticipants = listOf("u1"),
                    participants = listOf(member1),
                    payAmounts = PayAmountsUiState.OnePerson("u1", ""),
                )
            )
        val component =
            DefaultPaidAmountsComponent(
                parentUiState = parentUiState,
                onDone = {},
                cContext = cContext,
            )

        Then("it should not show 'null' in the amount field") {
          component.uiState.value.allParticipantAmounts.first().value shouldBe ""
        }
      }
    }
  }
}
