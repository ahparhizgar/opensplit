package com.opensplit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthRootScreen
import com.opensplit.features.auth.FakeAuthComponent
import com.opensplit.root.RootComponent

@Composable
fun App(root: RootComponent) {
    MaterialTheme {
        // Create a minimal Decompose CContext for the root component and use
        // the Decompose-based AuthComponent directly in the UI.
        Text(root::class.simpleName.orEmpty())
//        AuthRootScreen(
//            component = root,
//            modifier = Modifier.fillMaxSize(),
//        )
    }
}

@Preview
@Composable
private fun Preview() {
//    App(FakeRootComponent())
}

