package com.opensplit.root

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.component.CallBackNavigationOwner
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthMode
import com.opensplit.features.auth.FakeAuthComponent
import com.opensplit.features.household.HouseholdComponent
import kotlinx.serialization.Serializable
import org.koin.core.scope.Scope
import kotlin.reflect.KClass

interface RootComponent {
    val childStack: Value<ChildStack<*, Any>>

    interface Factory {
        fun create(context: CContext): RootComponent
    }
}

class DefaultRootComponent(
    cContext: CContext,
    private val componentProvider: ComponentProvider
) : RootComponent, CContext by cContext {

    private val navigation = StackNavigation<TopLevelDestinationConfig>()

    init {
        val navOwner = cContext.stackNavigationOwner as CallBackNavigationOwner
        @Suppress("UNCHECKED_CAST")
        navOwner.navigation = navigation as StackNavigation<Any>
    }

    override val childStack: Value<ChildStack<*, Any>> =
        childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = AuthComponent.Config(AuthMode.SignUp),
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(config: TopLevelDestinationConfig, cContext: CContext): Any {
        return when (config) {
            is AuthComponent.Config ->
                componentProvider.provide(AuthComponent.Factory::class)
                    .create(cContext, config)

            is HouseholdComponent.Config ->
                componentProvider.provide(HouseholdComponent.Factory::class)
                    .create(cContext, config)

            else -> error("Destination not defined in createChild")
        }
    }

    class Factory(
        private val componentProvider: ComponentProvider,
    ) : RootComponent.Factory {
        override fun create(context: CContext): RootComponent =
            DefaultRootComponent(context, componentProvider)
    }
}

class FakeRootComponent : RootComponent {
    override val childStack: Value<ChildStack<*, Any>> =
        MutableValue(
            ChildStack(
                active = Child.Created(
                    AuthComponent.Config(AuthMode.SignUp),
                    FakeAuthComponent()
                ),
                backStack = emptyList()
            )
        )
}

interface Destination

@Serializable
interface TopLevelDestinationConfig

interface ComponentProvider {
    fun <T : Any> provide(kClass: KClass<T>): T
}

class KoinComponentProvider(private val scope: Scope) : ComponentProvider {
    override fun <T : Any> provide(kClass: KClass<T>): T {
        return scope.get(clazz = kClass)
    }
}
