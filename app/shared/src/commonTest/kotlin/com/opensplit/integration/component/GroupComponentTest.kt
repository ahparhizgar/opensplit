package com.opensplit.integration.component

import com.arkivanov.essenty.lifecycle.create
import com.opensplit.component.TestCContext
import com.opensplit.component.defaultCContext
import com.opensplit.integration.group.createjoin.CreateGroupComponentFactory
import com.opensplit.integration.group.my.MyGroupsListComponentFactory
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

class GroupComponentTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a CreateGroupComponent") {
      var createComponent by testValue {
        koin.get<CreateGroupComponentFactory>().create(defaultCContext(createComponentContext()))
      }

      Then("initial fields are empty") {
        createComponent.uiState.value.let { state ->
          state.groupName shouldBe ""
          state.fieldErrors should beEmpty()
        }
      }

      When("submitting with an empty group name") {
        beforeEach { createComponent.submit() }
        Then("shows a validation error for name") {
          createComponent.uiState.value.let { state ->
            state.fieldErrors shouldNot beEmpty()
            state.fieldErrors["name"].shouldNotBeNull()
          }
        }
      }

      When("submitting with a valid group name") {
        beforeEach {
          createComponent.updateGroupName("Family Home")
          createComponent.submit()
        }
        Then("creates the group") {
          createComponent.uiState.value.let { state ->
            state.fieldErrors should beEmpty()
            state.generalError shouldBe null
          }
        }
      }

      When("typing then clearing the group name") {
        beforeEach {
          createComponent.updateGroupName("test")
          createComponent.updateGroupName("")
          createComponent.submit()
        }
        Then("still shows validation error on empty name") {
          createComponent.uiState.value.fieldErrors["name"].shouldNotBeNull()
        }
      }
    }

    Given("a MyGroupsListComponent") {
      var cContext by testValue { TestCContext() }
      var listComponent by testValue { koin.get<MyGroupsListComponentFactory>().create(cContext) }

      When("loading overview") {
        beforeEach {
          cContext.lifecycleRegistry.create()
          testCoroutineScheduler.advanceUntilIdle()
        }
        Then("loads groups") { listComponent.uiState.value.groups.shouldNotBeEmpty() }
      }

      When("leaving a group") {
        beforeEach {
          val id = "group-1"
          listComponent.leaveGroup(id).join()
        }
        Then("groups is removed from ui state") {
          listComponent.uiState.value.groups.map { it.id } shouldNotContain "group-1"
        }
      }
    }
  }
}
