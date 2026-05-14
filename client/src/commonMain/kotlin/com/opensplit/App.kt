package com.opensplit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.opensplit.features.auth.createAuthGateway
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.component.DefaultCContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.opensplit.features.auth.AuthRootScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Create a minimal Decompose CContext for the root component and use
        // the Decompose-based AuthComponent directly in the UI.
        val component = remember {
            val lifecycle = LifecycleRegistry()
            val cctx = DefaultCContext(lifecycle = lifecycle)
            DefaultAuthComponent.Factory(createAuthGateway()).create(cctx)
        }
        AuthRootScreen(
            component = component,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
