package com.opensplit.features.household

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

@Composable
fun HouseholdRootScreen(
    component: HouseholdComponent,
    modifier: Modifier = Modifier,
) {
    val householdId by component.householdId.collectAsState()
    Surface(modifier = modifier.fillMaxSize()) {
        if (householdId == null) {
            HouseholdSetupScreen(component = component)
        } else {
            HouseholdActiveScreen(householdId = householdId!!)
        }
    }
}

@Composable
fun HouseholdSetupScreen(component: HouseholdComponent) {
    val activeTab by component.activeTab.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp)
            .widthIn(max = 420.dp)
            .testTag("household-setup-shell"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Set up your household",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { component.useCreate() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("household-tab-create"),
            ) {
                Text(
                    text = "Create",
                    color = if (activeTab == HouseholdTab.Create)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
            }
            TextButton(
                onClick = { component.useJoin() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("household-tab-join"),
            ) {
                Text(
                    text = "Join",
                    color = if (activeTab == HouseholdTab.Join)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeTab) {
            HouseholdTab.Create -> {
                val state by component.createComponent.uiState.collectAsState()
                CreateHouseholdForm(
                    state = state,
                    onNameChange = component.createComponent::updateHouseholdName,
                )
                state.generalError?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("household-general-error"),
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { scope.launch { component.createComponent.submit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("household-submit"),
                    enabled = !state.isSubmitting,
                ) {
                    Text("Create household")
                }
            }
            HouseholdTab.Join -> {
                val state by component.joinComponent.uiState.collectAsState()
                JoinHouseholdForm(
                    state = state,
                    onCodeChange = component.joinComponent::updateInviteCode,
                )
                state.generalError?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("household-general-error"),
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { scope.launch { component.joinComponent.submit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("household-submit"),
                    enabled = !state.isSubmitting,
                ) {
                    Text("Join household")
                }
            }
        }
    }
}

@Composable
private fun CreateHouseholdForm(
    state: CreateHouseholdViewState,
    onNameChange: (String) -> Unit,
) {
    androidx.compose.material3.OutlinedTextField(
        value = state.householdName,
        onValueChange = onNameChange,
        label = { Text("Household name") },
        isError = state.fieldErrors.containsKey("name"),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("household-name"),
        singleLine = true,
    )
    state.fieldErrors["name"]?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("household-name-error"),
        )
    }
}

@Composable
private fun JoinHouseholdForm(
    state: JoinHouseholdViewState,
    onCodeChange: (String) -> Unit,
) {
    androidx.compose.material3.OutlinedTextField(
        value = state.inviteCode,
        onValueChange = onCodeChange,
        label = { Text("Invite code") },
        isError = state.fieldErrors.containsKey("inviteCode"),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("household-invite-code"),
        singleLine = true,
    )
    state.fieldErrors["inviteCode"]?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("household-invite-code-error"),
        )
    }
}

@Composable
fun HouseholdActiveScreen(householdId: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp)
            .testTag("household-active-shell"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Household ready",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Household ID: $householdId",
            modifier = Modifier.testTag("household-id-text"),
        )
    }
}

