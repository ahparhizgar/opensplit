package com.opensplit.component

import com.ahparhizgar.apicallerror.ApiCallError
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus


interface CContext : GenericComponentContext<CContext> {
    val stackNavigationOwner: StackNavigationOwner
}

interface StackNavigationOwner {
    val navigation: StackNavigation<Any>?
}

val CContext.navigation
    get() = stackNavigationOwner.navigation

class CallBackNavigationOwner() : StackNavigationOwner {
    override var navigation: StackNavigation<Any>? = null
}

class DefaultCContext(
    override val lifecycle: Lifecycle,
    override val stateKeeper: StateKeeper = StateKeeperDispatcher(),
    override val instanceKeeper: InstanceKeeper = InstanceKeeperDispatcher().also {
        lifecycle.doOnDestroy(it::destroy)
    },
    override val backHandler: BackHandler = BackDispatcher(),
    override val stackNavigationOwner: StackNavigationOwner = CallBackNavigationOwner()
) : CContext {
    override val componentContextFactory: ComponentContextFactory<CContext> =
        ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
            DefaultCContext(
                lifecycle,
                stateKeeper,
                instanceKeeper,
                backHandler,
                stackNavigationOwner
            )
        }
}

fun CContext.componentScope(): CoroutineScope {
    return CoroutineScope(Dispatchers.Main + SupervisorJob()).also {
        lifecycle.doOnDestroy {
            it.cancel("Component was destroyed.")
        }
    }
}

fun createDefaultComponentContext(componentContext: ComponentContext) =
    DefaultCContext(
        lifecycle = componentContext.lifecycle,
        stateKeeper = componentContext.stateKeeper,
        instanceKeeper = componentContext.instanceKeeper,
        backHandler = componentContext.backHandler,
    )

interface ApiCallShower : CContext {
    fun showApiCallError(error: ApiCallError)
}

fun ApiCallShower.apiCallScopeShower(): CoroutineScope {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is ApiCallError -> {
                // If this context can show API call errors, display it to the user
                showApiCallError(throwable)
            }

            else -> throw throwable
        }
    }
    return componentScope() + coroutineExceptionHandler
}

fun CContext.apiCallScope(): CoroutineScope {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is ApiCallError -> {
                // If the current CContext also implements ApiCallShower, delegate
                // to the UI-friendly error presenter. Otherwise, log a message so
                // that non-UI contexts don't silently swallow errors.
                if (this is ApiCallShower) {
                    this.showApiCallError(throwable)
                } else {
                    println("ApiCallError occurred: ${throwable.message ?: throwable}")
                }
            }

            else -> throw throwable
        }
    }
    return componentScope() + coroutineExceptionHandler
}
