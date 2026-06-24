package com.opensplit

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DecomposeSettings
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.opensplit.component.DefaultCContext
import com.opensplit.root.RootComponent
import org.koin.core.context.GlobalContext.startKoin
import javax.swing.SwingUtilities

fun main() {
    val lifecycle = LifecycleRegistry()

    val koin =
        startKoin {
            modules(appModule())
        }.koin

    val backDispatcher = BackDispatcher()
    val context = DefaultCContext(lifecycle = lifecycle, backHandler = backDispatcher)
    val root = runOnUiThread {
        koin.get<RootComponent.Factory>().create(context)
    }
    DecomposeSettings.settings = DecomposeSettings(duplicateConfigurationsEnabled = true)
    application {
        val windowState = rememberWindowState(width = 400.dp, height = 800.dp)
        LifecycleController(lifecycle, windowState)
        Window(
            onCloseRequest = ::exitApplication,
            title = "OpenSplit",
            onKeyEvent = {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                    backDispatcher.back()
                    true
                } else {
                    false
                }
            },
            state = windowState,
        ) {
            MenuBar {
                Menu("Navigation") {
                    Item(
                        "Back",
                        shortcut = KeyShortcut(Key.Escape)
                    ) {
                        backDispatcher.back()
                    }
                }
            }
            App(root = root)
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
