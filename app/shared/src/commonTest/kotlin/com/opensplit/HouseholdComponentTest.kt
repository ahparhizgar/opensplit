package com.opensplit

import com.opensplit.component.createDefaultComponentContext
import com.opensplit.features.household.DefaultCreateHouseholdComponent
import com.opensplit.features.household.DefaultHouseholdComponent
import com.opensplit.features.household.HouseholdComponent
import com.opensplit.features.household.HouseholdGateway
import com.opensplit.features.household.HouseholdTab
import com.opensplit.remote.RemoteException
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
import kotlinx.coroutines.test.runTest


class HouseholdComponentTest : BehaviorSpec({
    Given("a Household component – Create tab") {
        extensions(MainDispatcherExtension())
        val koin by integrationKoin()

        var component by testValue {
            koin.get<HouseholdComponent.Factory>().create(
                createDefaultComponentContext(createComponentContext()),
                HouseholdComponent.Config()
            )
        }

        Then("initial tab is Create and fields are empty") {
            component.activeTab.value shouldBe HouseholdTab.Create
            component.createComponent.uiState.value.let { state ->
                state.householdName shouldBe ""
                state.fieldErrors should beEmpty()
                state.householdId shouldBe null
            }
        }

        When(
            "submitting with an empty household name",
            { component.createComponent.submit() }
        ) {
            Then("shows a validation error for name") {
                component.createComponent.uiState.value.let { state ->
                    state.fieldErrors shouldNot beEmpty()
                    state.fieldErrors["name"].shouldNotBeNull()
                    state.householdId shouldBe null
                }
            }
        }

        When(
            "submitting with a valid household name",
            {
                component.createComponent.updateHouseholdName("Family Home")
                component.createComponent.submit()
            }
        ) {
            Then("creates the household and sets householdId") {
                component.createComponent.uiState.value.let { state ->
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
                component.createComponent.updateHouseholdName("test")
                component.createComponent.updateHouseholdName("")
                component.createComponent.submit()
            }
        ) {
            Then("still shows validation error on empty name") {
                component.createComponent.uiState.value.fieldErrors["name"].shouldNotBeNull()
            }
        }

        When(
            "switching to Join tab",
            { component.useJoin() }
        ) {
            Then("active tab is now Join") {
                component.activeTab.value shouldBe HouseholdTab.Join
            }
        }
    }

    Given("a Household component – Join tab") {
        extensions(MainDispatcherExtension())
        val koin by integrationKoin()

        var component by testValue {
            koin.get<HouseholdComponent.Factory>().create(
                createDefaultComponentContext(createComponentContext()),
                HouseholdComponent.Config()
            )
        }

        When(
            "submitting with an empty invite code",
            { component.joinComponent.submit() }
        ) {
            Then("shows a validation error for inviteCode") {
                component.joinComponent.uiState.value.let { state ->
                    state.fieldErrors shouldNot beEmpty()
                    state.fieldErrors["inviteCode"].shouldNotBeNull()
                    state.householdId shouldBe null
                }
            }
        }

        When(
            "submitting with a valid invite code",
            {
                component.joinComponent.updateInviteCode("invite-abc123")
                component.joinComponent.submit()
            }
        ) {
            Then("joins the household and sets householdId") {
                component.joinComponent.uiState.value.let { state ->
                    state.fieldErrors should beEmpty()
                    state.generalError shouldBe null
                    state.householdId.shouldNotBeNull()
                    koin.get<FakeHouseholdGateway>().joinCalls shouldBe 1
                }
            }
        }

        When(
            "switching to Create tab",
            { component.useCreate() }
        ) {
            Then("active tab is now Create") {
                component.activeTab.value shouldBe HouseholdTab.Create
            }
        }
    }

    Given("a CreateHouseholdComponent when gateway returns an error") {
        extensions(MainDispatcherExtension())

        var createComponent by testValue {
            DefaultCreateHouseholdComponent(
                context = createDefaultComponentContext(createComponentContext()),
                gateway = object : HouseholdGateway {
                    override suspend fun createHousehold(name: String) =
                        throw RemoteException(generalError = "Server error")

                    override suspend fun joinHousehold(inviteCode: String) =
                        throw RemoteException(generalError = "Server error")

                    override suspend fun loadOverview() =
                        throw RemoteException(generalError = "Server error")

                    override suspend fun switchHousehold(householdId: String) =
                        throw RemoteException(generalError = "Server error")

                    override suspend fun leaveHousehold(householdId: String) =
                        throw RemoteException(generalError = "Server error")
                }
            )
        }

        When(
            "the gateway throws RemoteException on create",
            {
                createComponent.updateHouseholdName("My Household")
                createComponent.submit()
            }
        ) {
            Then("shows the general error and householdId remains null") {
                createComponent.uiState.value.let { state ->
                    state.generalError.shouldNotBeNull()
                    state.householdId shouldBe null
                    state.isSubmitting shouldBe false
                }
            }
        }
    }

    Given("a Household component with an overview capable gateway") {
        extensions(MainDispatcherExtension())

        val gateway = FakeHouseholdGateway()

        var component by testValue {
            DefaultHouseholdComponent(
                context = createDefaultComponentContext(createComponentContext()),
                gateway = gateway,
            )
        }

        When("loading household overview") {
            runTest { component.loadOverview() }

            Then("exposes the active household from the overview") {
                component.overview.value.activeHouseholdId shouldBe "household-1"
                component.householdId.value shouldBe "household-1"
                gateway.loadOverviewCalls shouldBe 1
            }
        }

        When("switching the active household") {
            runTest {
                component.loadOverview()
                component.switchHousehold("household-2")
            }

            Then("updates the active household and marks the new household active") {
                component.overview.value.activeHouseholdId shouldBe "household-2"
                component.overview.value.households.first().isActive shouldBe true
                gateway.switchCalls shouldBe 1
            }
        }

        When("leaving the active household") {
            runTest {
                component.loadOverview()
                component.leaveHousehold("household-1")
            }

            Then("falls back to a safe landing state") {
                component.overview.value.activeHouseholdId shouldBe null
                component.overview.value.households shouldBe emptyList()
                gateway.leaveCalls shouldBe 1
            }
        }
    }
})
