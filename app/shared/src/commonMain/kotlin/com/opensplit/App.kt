package com.opensplit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthRootScreen
import com.opensplit.features.auth.FakeAuthComponent

@Composable
fun App(root: AuthComponent) {
    MaterialTheme {
        // Create a minimal Decompose CContext for the root component and use
        // the Decompose-based AuthComponent directly in the UI.
        AuthRootScreen(
            component = root,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    App(FakeAuthComponent())
}

