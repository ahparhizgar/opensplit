package com.opensplit

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DecomposeSettings
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.opensplit.component.DefaultCContext
import com.opensplit.root.RootComponent
import org.koin.core.context.GlobalContext.startKoin
import javax.swing.SwingUtilities
import androidx.compose.ui.unit.dp

fun main() {
    val lifecycle = LifecycleRegistry()

    val koin =
        startKoin {
            modules(appModule())
        }.koin

    val root = runOnUiThread {
        koin.get<RootComponent.Factory>().create(DefaultCContext(lifecycle = lifecycle))
    }
    DecomposeSettings.settings = DecomposeSettings(duplicateConfigurationsEnabled = true)
    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 900.dp)
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
