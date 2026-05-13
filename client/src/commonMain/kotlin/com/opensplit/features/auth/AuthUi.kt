package com.opensplit.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun AuthRootScreen(
    controller: AuthController,
    modifier: Modifier = Modifier,
) {
    val state = controller.state
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            if (state.session == null) {
                AuthEntryScreen(controller = controller)
            } else {
                HouseholdContextShell(state = controller.householdContextState()!!)
            }
        }
    }
}

@Composable
fun AuthEntryScreen(controller: AuthController) {
    val state = controller.state
    val title = if (state.mode == AuthMode.SignUp) "Create account" else "Sign in"
    val primaryAction = if (state.mode == AuthMode.SignUp) "Create account" else "Sign in"
    val toggleLabel = if (state.mode == AuthMode.SignUp) "Have an account? Sign in" else "Need an account? Sign up"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp)
            .widthIn(max = 420.dp)
            .testTag("auth-shell"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = controller::updateEmail,
            label = { Text("Email") },
            isError = state.fieldErrors.containsKey("email"),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-email"),
            singleLine = true,
        )
        state.fieldErrors["email"]?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("auth-email-error"))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = controller::updatePassword,
            label = { Text("Password") },
            isError = state.fieldErrors.containsKey("password"),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-password"),
            singleLine = true,
        )
        state.fieldErrors["password"]?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("auth-password-error"))
        }
        state.generalError?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("auth-general-error"))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = controller::submit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-submit"),
        ) {
            Text(primaryAction)
        }
        TextButton(
            onClick = {
                if (state.mode == AuthMode.SignUp) {
                    controller.useSignIn()
                } else {
                    controller.useSignUp()
                }
            },
            modifier = Modifier.testTag("auth-toggle-mode"),
        ) {
            Text(toggleLabel)
        }
    }
}

@Composable
fun HouseholdContextShell(state: HouseholdContextState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp)
            .testTag("household-shell"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = state.message, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Signed in as ${state.email}")
        Text(text = "No household setup is part of this story.")
    }
}
