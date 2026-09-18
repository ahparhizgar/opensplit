package com.opensplit

import com.arkivanov.essenty.lifecycle.create
import com.opensplit.component.TestCContext
import com.opensplit.component.defaultCContext
import com.opensplit.features.household.createjoin.CreateHouseholdComponent
import com.opensplit.features.household.createjoin.CreateHouseholdComponentFactory
import com.opensplit.features.household.my.MyHouseholdsListComponent
import com.opensplit.features.household.my.MyHouseholdsListComponentFactory
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.createComponentContext
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot

class HouseholdComponentTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a CreateHouseholdComponent") {
      var createComponent by testValue {
        koin
            .get<CreateHouseholdComponentFactory>()
            .create(defaultCContext(createComponentContext()))
      }

      Then("initial fields are empty") {
        createComponent.uiState.value.let { state ->
          state.householdName shouldBe ""
          state.fieldErrors should beEmpty()
        }
      }

      When("submitting with an empty household name") {
        beforeEach { createComponent.submit() }
        Then("shows a validation error for name") {
          createComponent.uiState.value.let { state ->
            state.fieldErrors shouldNot beEmpty()
            state.fieldErrors["name"].shouldNotBeNull()
          }
        }
      }

      When("submitting with a valid household name") {
        beforeEach {
          createComponent.updateHouseholdName("Family Home")
          createComponent.submit()
        }
        Then("creates the household") {
          createComponent.uiState.value.let { state ->
            state.fieldErrors should beEmpty()
            state.generalError shouldBe null
          }
        }
      }

      When("typing then clearing the household name") {
        beforeEach {
          createComponent.updateHouseholdName("test")
          createComponent.updateHouseholdName("")
          createComponent.submit()
        }
        Then("still shows validation error on empty name") {
          createComponent.uiState.value.fieldErrors["name"].shouldNotBeNull()
        }
      }
    }

    Given("a MyHouseholdsListComponent") {
      var cContext by testValue { TestCContext() }
      var listComponent by testValue {
        koin.get<MyHouseholdsListComponentFactory>().create(cContext)
      }

      When("loading overview") {
        beforeEach {
          cContext.lifecycleRegistry.create()
          testCoroutineScheduler.advanceUntilIdle()
        }
        Then("loads households") { listComponent.uiState.value.households.shouldNotBeEmpty() }
      }

      When("leaving a household") {
        beforeEach {
          val id = "household-1"
          listComponent.leaveHousehold(id).join()
        }
        Then("households is removed from ui state") {
          listComponent.uiState.value.households.map { it.id } shouldNotContain "household-1"
        }
      }
    }
  }
}
