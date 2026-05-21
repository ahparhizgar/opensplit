package com.opensplit

import com.opensplit.component.createDefaultComponentContext
import com.opensplit.features.auth.AuthMode
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.util.When
import com.opensplit.util.createComponentContext
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class AuthComponentTest : BehaviorSpec({
    Given("an Auth component") {
        var gateway by testValue { FakeAuthGateway() }
        var component by testValue {
            DefaultAuthComponent.Factory(gateway)
                .create(createDefaultComponentContext(createComponentContext()))
        }

        When(
            "using sign up with invalid then valid input",
            {
                component.useSignUp()
                component.updateEmail("bad-email")
                component.updatePassword("short")
                component.submit()
            }
        ) {
            Then("uses shared validation and routes on success") {
                component.uiState.value.let { state ->
                    state.fieldErrors.isNotEmpty().shouldBeTrue()
                    state.mode shouldBe AuthMode.SignUp
                    state.session shouldBe null
                }
            }

            When(
                "providing valid credentials",
                {
                    component.updateEmail("valid@example.com")
                    component.updatePassword("password123")
                    component.submit()
                }
            ) {
                Then("routes to authenticated session") {
                    component.uiState.value.let { state ->
                        state.fieldErrors.isEmpty().shouldBeTrue()
                        val session = state.session.shouldNotBeNull()
                        session.email shouldBe "valid@example.com"
                    }
                }
            }
        }

        When(
            "submitting valid credentials",
            {
                component.updateEmail("amir@example.com")
                component.updatePassword("password123")
                component.submit()
            }
        ) {
            Then("routes to household context after valid submission") {
                component.uiState.value.let { state ->
                    val session = state.session.shouldNotBeNull()
                    session.email shouldBe "amir@example.com"
                    gateway.signUpCalls shouldBe 1
                }
            }
        }
    }
})
