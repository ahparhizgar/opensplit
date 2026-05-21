package com.opensplit.util

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.kotest.core.test.TestScope
import kotlinx.coroutines.test.runTest

fun FunSpecRootScope.testComponent(name: String, test: suspend TestComponentScope.() -> Unit) {
    val lifecycleRegistry = LifecycleRegistry()
    val c = createComponentContext(lifecycle = lifecycleRegistry)
    test(name = name) {
        val scope = DefaultTestComponentScope(lifecycleRegistry, c, this)
        with(scope) {
            runTest {
                test()
            }
        }
    }
}

interface ComponentScope {
    val lifecycleRegistry: LifecycleRegistry
    val context: ComponentContext
}

interface TestComponentScope : TestScope, ComponentScope

class DefaultTestComponentScope(
    override val lifecycleRegistry: LifecycleRegistry,
    override val context: ComponentContext,
    testScope: TestScope
) : TestComponentScope, TestScope by testScope

fun createComponentContext(
    lifecycle: Lifecycle = LifecycleRegistry(),
    stateKeeper: StateKeeper = StateKeeperDispatcher(),
    instanceKeeper: InstanceKeeper = InstanceKeeperDispatcher(),
): ComponentContext {
    return DefaultComponentContext(
        lifecycle = lifecycle,
        stateKeeper = stateKeeper,
        instanceKeeper = instanceKeeper,
    )
}
