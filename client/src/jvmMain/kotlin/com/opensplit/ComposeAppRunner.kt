package com.opensplit

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

object ComposeAppRunner {
    fun launch(title: String = "opensplit") {
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = title,
            ) {
                App()
            }
        }
    }
}
