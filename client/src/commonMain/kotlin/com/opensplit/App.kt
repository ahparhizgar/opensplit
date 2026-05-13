package com.opensplit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.opensplit.features.auth.AuthController
import com.opensplit.features.auth.createAuthGateway
import com.opensplit.features.auth.AuthRootScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        val controller = remember { AuthController(createAuthGateway()) }
        AuthRootScreen(
            controller = controller,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
