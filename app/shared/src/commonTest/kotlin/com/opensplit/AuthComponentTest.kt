package com.opensplit

import com.opensplit.component.createDefaultComponentContext
import com.opensplit.features.auth.AuthComponent
import com.opensplit.util.And
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.When
import com.opensplit.util.createComponentContext
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot

class AuthComponentTest :
    BehaviorSpec({
      extensions(MainDispatcherExtension())
      Given("an Auth component") {
        val koin by integrationKoin()
        var component by testValue {
          koin
              .get<AuthComponent.Factory>()
              .create(createDefaultComponentContext(createComponentContext()))
        }

        When(
            "navigating to sign up and using invalid input",
            {
              component.welcome().onSignUpClicked()

              with(component.signUp()) {
                onEmailChanged("bad-email")
                onPasswordChanged("short")
                onDoneClicked()
              }
            },
        ) {
          Then("shows validation errors") {
            component.signUp().state.value.let { state -> state.fieldErrors shouldNot beEmpty() }
          }

          And(
              "providing valid credentials",
              {
                with(component.signUp()) {
                  onEmailChanged("valid@example.com")
                  onPasswordChanged("password123")
                  onDoneClicked()
                }
              },
          ) {
            Then("calls gateway") { koin.get<FakeAuthGateway>().signUpCalls shouldBe 1 }
          }
        }
      }
    })

fun AuthComponent.welcome() = (stack.value.active.instance as AuthComponent.Child.Welcome).component

fun AuthComponent.signUp() = (stack.value.active.instance as AuthComponent.Child.SignUp).component

fun AuthComponent.login() = (stack.value.active.instance as AuthComponent.Child.Login).component

fun AuthComponent.resetPassword() =
    (stack.value.active.instance as AuthComponent.Child.ResetPassword).component
