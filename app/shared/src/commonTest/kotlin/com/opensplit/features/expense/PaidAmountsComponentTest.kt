package com.opensplit.features.expense

import com.opensplit.domain.FakeGroupFactory
import com.opensplit.domain.FakeMemberFactory
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PaidAmountsComponentTest : BehaviorSpec() {
  init {
    Given("a DefaultPaidAmountsComponent") {
      val member1 = FakeMemberFactory.create(userId = "u1", name = "User 1")
      val group = FakeGroupFactory.create(members = listOf(member1))

      When("initialized with OnePerson and null amount") {
        val component =
            DefaultPaidAmountsComponent(
                initial = PayAmounts.OnePerson(userId = "u1", amount = null),
                group = group,
                onDone = {},
            )

        Then("it should not show 'null' in the amount field") {
          component.uiState.value.allParticipantAmounts.first().value shouldBe ""
        }
      }
    }
  }
}
