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
import kotlinx.serialization.Serializable
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import kotlin.reflect.KClass

interface RootComponent {
    val childStack: Value<ChildStack<*, Any>>
}

class DefaultRootComponent(
    cContext: CContext,
    private val componentProvider: ComponentProvider
) : RootComponent, CContext by cContext {

    private val navigation = StackNavigation<DestinationConfig>()

    init {
        val navOwner = cContext.stackNavigationOwner as CallBackNavigationOwner
        @Suppress("UNCHECKED_CAST")
        navOwner.navigation = navigation as StackNavigation<Any>
    }

    override val childStack: Value<ChildStack<*, Any>> =
        childStack(
            source = navigation,
            serializer = null, // DestinationConfig.serializer(),
            initialConfiguration = AuthComponent.Config(AuthMode.SignUp),
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(config: DestinationConfig, cContext: CContext): Any {
        // Use the provided componentClass to create the component instance.
        @Suppress("UNCHECKED_CAST")
        return componentProvider(config.componentClass as KClass<Any>, cContext, config)
    }
}

class FakeRootComponent : RootComponent {
    override val childStack: Value<ChildStack<*, Any>> =
        MutableValue(
            ChildStack(
                active = Child.Created(
                    AuthComponent.Config(
                        AuthMode.SignUp
                    ), FakeAuthComponent()
                ), backStack = emptyList()
            )
        )

}

interface Destination

@Serializable
interface DestinationConfig {
    /** The KClass of the component that should be created for this destination. */
    val componentClass: KClass<out Any>
}

interface ComponentProvider {
    operator fun <T : Any> invoke(
        kClass: KClass<T>,
        cContext: CContext,
        config: DestinationConfig
    ): T
}

class KoinComponentProvider(private val scope: Scope) : ComponentProvider {
    override fun <T : Any> invoke(
        kClass: KClass<T>,
        cContext: CContext,
        config: DestinationConfig
    ): T {
        return scope.get(clazz = kClass, parameters = { parametersOf(cContext, config) })
    }
}
