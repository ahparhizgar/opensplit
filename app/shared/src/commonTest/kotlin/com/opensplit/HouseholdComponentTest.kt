package com.opensplit

import com.opensplit.component.createDefaultComponentContext
import com.opensplit.features.household.DefaultHouseholdComponent
import com.opensplit.features.household.HouseholdComponent
import com.opensplit.features.household.HouseholdMode
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

class HouseholdComponentTest : BehaviorSpec({
    Given("a Household component in Create mode") {
        extensions(MainDispatcherExtension())
        val koin by integrationKoin()

        var component by testValue {
            koin.get<DefaultHouseholdComponent> {
                parametersOf(
                    createDefaultComponentContext(createComponentContext()),
                    HouseholdComponent.Config(accessToken = "test-token", mode = HouseholdMode.Create)
                )
            }
        }

        Then("initial state has Create mode and empty fields") {
            component.uiState.value.let { state ->
                state.mode shouldBe HouseholdMode.Create
                state.householdName shouldBe ""
                state.fieldErrors should beEmpty()
                state.householdId shouldBe null
            }
        }

        When(
            "submitting with an empty household name",
            { component.submit() }
        ) {
            Then("shows a validation error for name") {
                component.uiState.value.let { state ->
                    state.fieldErrors shouldNot beEmpty()
                    state.fieldErrors["name"].shouldNotBeNull()
                    state.householdId shouldBe null
                }
            }
        }

        When(
            "submitting with a valid household name",
            {
                component.updateHouseholdName("Family Home")
                component.submit()
            }
        ) {
            Then("creates the household and sets householdId") {
                component.uiState.value.let { state ->
                    state.fieldErrors should beEmpty()
                    state.generalError shouldBe null
                    state.householdId.shouldNotBeNull()
                    koin.get<FakeHouseholdGateway>().createCalls shouldBe 1
                }
            }
        }

        When(
            "typing then clearing the household name",
            {
                component.updateHouseholdName("test")
                component.updateHouseholdName("")
                component.submit()
            }
        ) {
            Then("still shows validation error on empty name") {
                component.uiState.value.fieldErrors["name"].shouldNotBeNull()
            }
        }

        When(
            "switching to Join mode",
            { component.useJoin() }
        ) {
            Then("mode is now Join and errors are cleared") {
                component.uiState.value.let { state ->
                    state.mode shouldBe HouseholdMode.Join
                    state.fieldErrors should beEmpty()
                }
            }
        }
    }

    Given("a Household component in Join mode") {
        extensions(MainDispatcherExtension())
        val koin by integrationKoin()

        var component by testValue {
            koin.get<DefaultHouseholdComponent> {
                parametersOf(
                    createDefaultComponentContext(createComponentContext()),
                    HouseholdComponent.Config(accessToken = "test-token", mode = HouseholdMode.Join)
                )
            }
        }

        When(
            "submitting with an empty invite code",
            { component.submit() }
        ) {
            Then("shows a validation error for inviteCode") {
                component.uiState.value.let { state ->
                    state.fieldErrors shouldNot beEmpty()
                    state.fieldErrors["inviteCode"].shouldNotBeNull()
                    state.householdId shouldBe null
                }
            }
        }

        When(
            "submitting with a valid invite code",
            {
                component.updateInviteCode("invite-abc123")
                component.submit()
            }
        ) {
            Then("joins the household and sets householdId") {
                component.uiState.value.let { state ->
                    state.fieldErrors should beEmpty()
                    state.generalError shouldBe null
                    state.householdId.shouldNotBeNull()
                    koin.get<FakeHouseholdGateway>().joinCalls shouldBe 1
                }
            }
        }

        When(
            "switching to Create mode",
            { component.useCreate() }
        ) {
            Then("mode is now Create and errors are cleared") {
                component.uiState.value.let { state ->
                    state.mode shouldBe HouseholdMode.Create
                    state.fieldErrors should beEmpty()
                }
            }
        }
    }

    Given("a Household component when gateway returns an error") {
        extensions(MainDispatcherExtension())
        val koin by integrationKoin()

        var component by testValue {
            koin.get<DefaultHouseholdComponent> {
                parametersOf(
                    createDefaultComponentContext(createComponentContext()),
                    HouseholdComponent.Config(accessToken = "test-token", mode = HouseholdMode.Create)
                )
            }
        }

        When(
            "the gateway throws HouseholdRemoteException",
            {
                val fakeGateway = koin.get<FakeHouseholdGateway>()
                // Override with a failing gateway by testing the component directly with a failing gateway
                val failingComponent = DefaultHouseholdComponent(
                    context = createDefaultComponentContext(createComponentContext()),
                    config = HouseholdComponent.Config(accessToken = "test-token"),
                    gateway = object : com.opensplit.features.household.HouseholdGateway {
                        override suspend fun createHousehold(
                            name: String,
                            accessToken: String,
                        ) = throw com.opensplit.features.household.HouseholdRemoteException(
                            generalError = "Server error"
                        )
                        override suspend fun joinHousehold(
                            inviteCode: String,
                            accessToken: String,
                        ) = throw com.opensplit.features.household.HouseholdRemoteException(
                            generalError = "Server error"
                        )
                    }
                )
                component = failingComponent
                component.updateHouseholdName("My Household")
                component.submit()
            }
        ) {
            Then("shows the general error and householdId remains null") {
                component.uiState.value.let { state ->
                    state.generalError.shouldNotBeNull()
                    state.householdId shouldBe null
                    state.isSubmitting shouldBe false
                }
            }
        }
    }
})
