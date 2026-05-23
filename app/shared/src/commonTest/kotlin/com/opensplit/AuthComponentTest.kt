package com.opensplit

import com.opensplit.component.createDefaultComponentContext
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthMode
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.When
import com.opensplit.util.createComponentContext
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.koin.core.parameter.parametersOf

class AuthComponentTest : BehaviorSpec({
    Given("an Auth component") {
        extensions(MainDispatcherExtension())
        val koin by integrationKoin()

        var component by testValue {
            koin.get<DefaultAuthComponent> {
                parametersOf(
                    createDefaultComponentContext(createComponentContext()),
                    AuthComponent.Config(AuthMode.SignUp)
                )
            }
        }

        When(
            "using sign up with invalid input",
            {
                component.useSignUp()
                component.updateEmail("bad-email")
                component.updatePassword("short")
                component.submit()
            }
        ) {
            Then("uses shared validation and routes on success") {
                component.uiState.value.let { state ->
                    state.fieldErrors shouldNot beEmpty()
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
                        state.fieldErrors should beEmpty()
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
                    koin.get<FakeAuthGateway>().signUpCalls shouldBe 1
                }
            }
        }
    }
})
