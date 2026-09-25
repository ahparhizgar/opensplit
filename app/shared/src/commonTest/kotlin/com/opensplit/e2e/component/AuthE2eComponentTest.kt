package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.integration.auth.AuthComponent
import com.opensplit.integration.component.assertLogin
import com.opensplit.integration.component.assertWelcome
import com.opensplit.integration.group.my.MyGroupsListComponent
import com.opensplit.root.RootComponent
import com.opensplit.root.RootComponentFactory
import com.opensplit.splash.SplashDestination
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.types.shouldBeInstanceOf

class E2EAuthTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    Given("app opens for first time") {
      val koin by integrationKoin()
      val context by testValue { TestCContext() }
      var root by testValue { koin.get<RootComponentFactory>().create(context) }
      Then("shows welcome screen") { root.assertSplash() }
      When("waiting") {
        beforeEach { testCoroutineScheduler.advanceUntilIdle() }
        Then("shows auth screen") { root.assertAuth().assertWelcome() }
        And("click on login") {
          beforeEach { root.assertAuth().assertWelcome().onLoginClicked() }
          Then("shows login screen") { root.assertAuth().assertLogin() }
          And("entering valid credentials") {
            beforeEach {
              root.assertAuth().assertLogin().let {
                it.onEmailChanged("valid@example.com")
                it.onPasswordChanged("password123")
                it.onLoginClicked()
              }
              testCoroutineScheduler.advanceUntilIdle()
            }
            Then("navigates to MyGroupsListComponent") { root.assertMyGroupsList() }
          }
        }
      }
    }
  }
}

private fun RootComponent.activeInstance(): Any = childStack.value.active.instance

fun RootComponent.assertSplash() = activeInstance().shouldBeInstanceOf<SplashDestination>()

fun RootComponent.assertAuth() = activeInstance().shouldBeInstanceOf<AuthComponent>()

fun RootComponent.assertMyGroupsList() =
    activeInstance().shouldBeInstanceOf<MyGroupsListComponent>()
