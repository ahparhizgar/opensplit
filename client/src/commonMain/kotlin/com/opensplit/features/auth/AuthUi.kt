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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AuthRootScreen(
    component: AuthComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.uiState.collectAsState()
    val session = state.session
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            if (session == null) {
                AuthEntryScreen(component = component)
            } else {
                HouseholdContextShell(
                    state = HouseholdContextState(
                        message = "Authenticated household context",
                        email = session.email,
                        householdId = session.householdId,
                    )
                )
            }
        }
    }
}

data class HouseholdContextState(
    val message: String,
    val email: String,
    val householdId: String? = null,
)
@Composable
fun AuthEntryScreen(component: AuthComponent) {
    val state by component.uiState.collectAsState(initial = com.opensplit.features.auth.AuthViewState())
    val scope = rememberCoroutineScope()
    val title = if (state.mode == AuthMode.SignUp) "Create account" else "Sign in"
    val primaryAction = authSubmitLabel(state.mode)
    val toggleLabel = authToggleLabel(state.mode)

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
            onValueChange = component::updateEmail,
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
            onValueChange = component::updatePassword,
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
            onClick = { scope.launch { component.submit() } },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-submit"),
            enabled = !state.isSubmitting,
        ) {
            Text(primaryAction)
        }
        TextButton(
            onClick = {
                if (state.mode == AuthMode.SignUp) {
                    component.useSignIn()
                } else {
                    component.useSignUp()
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
