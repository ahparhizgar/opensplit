package com.opensplit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthRootScreen
import com.opensplit.root.FakeRootComponent
import com.opensplit.root.RootComponent

@Composable
fun App(root: RootComponent) {
    MaterialTheme {
        Children(root.childStack) {
            when (val child = it.instance) {
                is AuthComponent -> AuthRootScreen(
                    component = child,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    App(FakeRootComponent())
}

