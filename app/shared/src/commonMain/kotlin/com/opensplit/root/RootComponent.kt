package com.opensplit.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.component.CallBackNavigationOwner
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthMode
import kotlinx.serialization.Serializable
import org.koin.core.parameter.parametersOf
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

    private val navigation = StackNavigation<DestinationConfig>()

    init {
        val navOwner = cContext.stackNavigationOwner as CallBackNavigationOwner
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
        return config.createComponent(componentProvider, cContext)
    }
}

interface Destination

@Serializable
interface DestinationConfig {
    fun createComponent(componentProvider: ComponentProvider, cContext: CContext): Any
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
