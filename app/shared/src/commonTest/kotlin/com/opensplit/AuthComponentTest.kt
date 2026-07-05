package com.opensplit

import com.opensplit.component.createDefaultComponentContext
import com.opensplit.component.fakeStack
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.household.my.MyHouseholdsListComponent
import com.opensplit.util.And
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.When
import com.opensplit.util.createComponentContext
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.shouldNot
import io.kotest.matchers.types.shouldBeInstanceOf

class AuthComponentTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    Given("an Auth component") {
      val koin by integrationKoin()
      val cContext by testValue { createDefaultComponentContext(createComponentContext()) }
      var component by testValue { koin.get<AuthComponent.Factory>().create(cContext) }

      When(
          "navigating to sign up and using invalid input",
          {
            component.assertWelcome().onSignUpClicked()

            with(component.assertSignUp()) {
              onEmailChanged("bad-email")
              onPasswordChanged("short")
              onDoneClicked()
            }
          },
      ) {
        Then("shows validation errors") {
          component.assertSignUp().state.value.let { state ->
            state.fieldErrors shouldNot beEmpty()
          }
        }

        And(
            "providing valid credentials",
            {
              with(component.assertSignUp()) {
                onEmailChanged("valid@example.com")
                onPasswordChanged("password123")
                onDoneClicked()
                testCoroutineScheduler.advanceUntilIdle()
              }
            },
        ) {
          Then("calls gateway") {
            cContext.fakeStack() shouldContainExactly listOf(MyHouseholdsListComponent.Config)
          }
        }
      }
    }
  }
}

private fun AuthComponent.activeInstance(): AuthComponent.Child = stack.value.active.instance

fun AuthComponent.assertWelcome() =
    activeInstance().shouldBeInstanceOf<AuthComponent.Child.Welcome>().component

fun AuthComponent.assertSignUp() =
    activeInstance().shouldBeInstanceOf<AuthComponent.Child.SignUp>().component

fun AuthComponent.assertLogin() =
    activeInstance().shouldBeInstanceOf<AuthComponent.Child.Login>().component

fun AuthComponent.assertResetPassword() =
    activeInstance().shouldBeInstanceOf<AuthComponent.Child.ResetPassword>().component
