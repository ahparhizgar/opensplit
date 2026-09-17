package com.opensplit

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DecomposeSettings
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.opensplit.component.DefaultCContext
import com.opensplit.di.appModule
import com.opensplit.root.RootComponent
import com.opensplit.root.RootComponentFactory
import kotlinx.browser.document
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.w3c.dom.events.Event

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  val lifecycle = LifecycleRegistry()

  val koin = startKoin {
    modules(
        module {
          single {
            DataDir(DataDir.DEFAULT)
          }
        }
    )
    modules(appModule())
  }
      .koin

  val backDispatcher = BackDispatcher()
  val context = DefaultCContext(lifecycle = lifecycle, backHandler = backDispatcher)
  val root = koin.get<RootComponentFactory>().create(context)
  DecomposeSettings.settings = DecomposeSettings(duplicateConfigurationsEnabled = true)

  // Attach the LifecycleRegistry to document
  lifecycle.attachToDocument()

  ComposeViewport { App(root) }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun isDocumentHidden(): Boolean =
    js("Boolean(document.hidden || document.visibilityState === 'hidden')")

// Attaches the LifecycleRegistry to the document dynamically
private fun LifecycleRegistry.attachToDocument() {
  fun updateVisibility() {
    if (!isDocumentHidden()) {
      resume()
    } else {
      stop()
    }
  }

  updateVisibility()

  document.addEventListener("visibilitychange") { _: Event ->
    updateVisibility()
  }
}
