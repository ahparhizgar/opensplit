package com.opensplit

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.start
import com.opensplit.component.CContext
import com.opensplit.component.createDefaultComponentContext
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.FakeAuthComponent
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.household.FakeHouseholdComponent
import com.opensplit.features.household.HouseholdComponent
import com.opensplit.root.ComponentProvider
import com.opensplit.root.DefaultRootComponent
import com.opensplit.root.RootComponent
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.createComponentContext
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class RootComponentTest : BehaviorSpec({
    extensions(MainDispatcherExtension())
    Given("a RootComponent with no saved token") {
        val lifecycleRegistry = LifecycleRegistry()
        val componentContext = createComponentContext(lifecycle = lifecycleRegistry)

        val root = DefaultRootComponent(
            cContext = createDefaultComponentContext(componentContext),
            componentProvider = TestComponentProvider(
                authFactory = FakeAuthComponentFactory(),
                householdFactory = FakeHouseholdComponentFactory(),
            ),
            tokenStorage = TokenStorage { null },
        )

        When("the component initializes") {
            lifecycleRegistry.start()
            testCoroutineScheduler.advanceUntilIdle()
            Then("it shows the auth screen, not household data") {
                val child = root.childStack.value
                child.active.configuration.shouldBeInstanceOf<AuthComponent.Config>()
            }
        }
    }

    Given("a RootComponent with a saved token") {
        val testDispatcher: TestDispatcher = StandardTestDispatcher()
        extensions(MainDispatcherExtension(testDispatcher))
        val lifecycleRegistry = LifecycleRegistry()
        val componentContext = createComponentContext(lifecycle = lifecycleRegistry)

        val root = DefaultRootComponent(
            cContext = createDefaultComponentContext(componentContext),
            componentProvider = TestComponentProvider(
                authFactory = FakeAuthComponentFactory(),
                householdFactory = FakeHouseholdComponentFactory(),
            ),
            tokenStorage = TokenStorage { "test-token" },
        )

        When("the component initializes") {
            lifecycleRegistry.start()
            testDispatcher.scheduler.advanceUntilIdle()
            Then("it navigates to the household screen") {
                val child = root.childStack.value
                child.active.configuration.shouldBeInstanceOf<HouseholdComponent.Config>()
            }
        }
    }
})

private class TokenStorage(private val token: () -> String?) : com.opensplit.features.auth.TokenStorage {
    override suspend fun saveAccessToken(token: String) {}
    override suspend fun getAccessToken(): String? = token()
    override suspend fun clearAccessToken() {}
}

private class FakeAuthComponentFactory : AuthComponent.Factory {
    override fun create(cContext: CContext, config: AuthComponent.Config): AuthComponent =
        FakeAuthComponent()
}

private class FakeHouseholdComponentFactory : HouseholdComponent.Factory {
    override fun create(cContext: CContext, config: HouseholdComponent.Config): HouseholdComponent =
        FakeHouseholdComponent()
}

private class TestComponentProvider(
    private val authFactory: AuthComponent.Factory,
    private val householdFactory: HouseholdComponent.Factory,
) : ComponentProvider {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> provide(kClass: kotlin.reflect.KClass<T>): T {
        return when (kClass) {
            AuthComponent.Factory::class -> authFactory
            HouseholdComponent.Factory::class -> householdFactory
            else -> error("Unknown factory: $kClass")
        } as T
    }
}