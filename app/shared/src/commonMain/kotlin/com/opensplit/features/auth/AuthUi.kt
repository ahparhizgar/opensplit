package com.opensplit.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.opensplit.ui.AdaptiveLayoutSizeClass
import com.opensplit.ui.adaptiveLayoutSizeClass

@Composable
fun AuthRootScreen(
    component: AuthComponent,
    modifier: Modifier = Modifier,
) {
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            AuthEntryScreen(component = component)
        }
    }
}

@Composable
fun AuthEntryScreen(component: AuthComponent) {
    val state by component.uiState.collectAsState(initial = AuthViewState())
    val scope = rememberCoroutineScope()
    val title = if (state.mode == AuthMode.SignUp) "Create account" else "Sign in"
    val primaryAction = authSubmitLabel(state.mode)
    val toggleLabel = authToggleLabel(state.mode)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val sizeClass = adaptiveLayoutSizeClass(maxWidth)
        when (sizeClass) {
            AdaptiveLayoutSizeClass.Expanded -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1120.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AuthHeroPanel(
                        title = title,
                        sizeClass = sizeClass,
                        modifier = Modifier.weight(1f),
                    )
                    AuthFormPanel(
                        state = state,
                        title = title,
                        primaryAction = primaryAction,
                        toggleLabel = toggleLabel,
                        onEmailChange = component::updateEmail,
                        onPasswordChange = component::updatePassword,
                        onSubmit = { scope.launch { component.submit() } },
                        onToggleMode = {
                            if (state.mode == AuthMode.SignUp) {
                                component.useSignIn()
                            } else {
                                component.useSignUp()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 420.dp),
                    )
                }
            }

            AdaptiveLayoutSizeClass.Medium, AdaptiveLayoutSizeClass.Compact -> {
                AuthFormPanel(
                    state = state,
                    title = title,
                    primaryAction = primaryAction,
                    toggleLabel = toggleLabel,
                    onEmailChange = component::updateEmail,
                    onPasswordChange = component::updatePassword,
                    onSubmit = { scope.launch { component.submit() } },
                    onToggleMode = {
                        if (state.mode == AuthMode.SignUp) {
                            component.useSignIn()
                        } else {
                            component.useSignUp()
                        }
                    },
                    modifier = Modifier
                        .widthIn(max = if (sizeClass.isMedium) 560.dp else 420.dp)
                        .testTag("auth-shell"),
                )
            }
        }
    }
}

@Composable
private fun AuthHeroPanel(
    title: String,
    sizeClass: AdaptiveLayoutSizeClass,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = "OpenSplit", style = MaterialTheme.typography.displaySmall)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = when (sizeClass) {
                AdaptiveLayoutSizeClass.Expanded -> "Keep your household context visible while you sign in on a desktop-sized screen."
                AdaptiveLayoutSizeClass.Medium -> "A wider layout keeps the auth form comfortable on tablets and laptops."
                AdaptiveLayoutSizeClass.Compact -> "Keep moving with a focused sign-in surface."
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "• Sign in or create an account")
            Text(text = "• Keep household context ready for the next step")
            Text(text = "• Works across Android, desktop, and web")
        }
    }
}

@Composable
private fun AuthFormPanel(
    state: AuthViewState,
    title: String,
    primaryAction: String,
    toggleLabel: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = 560.dp)
            .testTag("auth-shell"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
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
            onValueChange = onPasswordChange,
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
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-submit"),
            enabled = !state.isSubmitting,
        ) {
            Text(primaryAction)
        }
        TextButton(
            onClick = onToggleMode,
            modifier = Modifier.testTag("auth-toggle-mode"),
        ) {
            Text(toggleLabel)
        }
    }
}
