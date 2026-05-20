package com.opensplit

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.opensplit.component.DefaultCContext
import com.opensplit.features.auth.DefaultAuthComponent
import org.koin.core.context.GlobalContext.startKoin
import javax.swing.SwingUtilities

fun main() {
    val lifecycle = LifecycleRegistry()

    val koin =
        startKoin {
            modules(appModule())
        }.koin

    val root = runOnUiThread {
        koin.get<DefaultAuthComponent.Factory>().create(
            context = DefaultCContext(lifecycle = lifecycle),
        )
    }
    application {
        val windowState = rememberWindowState()
        LifecycleController(lifecycle, windowState)

        Window(
            onCloseRequest = ::exitApplication,
            title = "OpenSplit",
        ) {
            App(root)
        }
    }
}

internal fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (e: Throwable) {
            error = e
        }
    }

    error?.also { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}
