package com.opensplit.features.household

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import kotlinx.coroutines.launch

@Composable
fun HouseholdRootScreen(
    component: HouseholdComponent,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = component.childStack,
        modifier = modifier,
    ) {
        when (val child = it.instance) {
            is HouseholdComponent.Child.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is HouseholdComponent.Child.CreateJoin ->
                CreateJoinHouseholdScreen(child.component)

            is HouseholdComponent.Child.List ->
                MyHouseholdsListScreen(
                    component = child.component,
                    onHouseholdClick = { id ->
                        component.onHouseholdClick(id)
                    }
                )

            is HouseholdComponent.Child.Detail -> HouseholdDetailScreen(child.component)
        }
    }
}

@Composable
fun CreateJoinHouseholdScreen(
    component: CreateJoinHouseholdComponent,
    modifier: Modifier = Modifier,
) {
    val activeTab by component.activeTab.collectAsState()
    val scope = rememberCoroutineScope()
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        HouseholdSetupContent(
            createJoinComponent = component,
            activeTab = activeTab,
            scope = scope,
            width = 420.dp,
            modifier = Modifier.widthIn(max = 420.dp),
        )
    }
}

@Composable
fun MyHouseholdsListScreen(
    component: MyHouseholdsListComponent,
    onHouseholdClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by component.isLoading.collectAsState()
    val overview by component.overview.collectAsState()

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val scope = rememberCoroutineScope()
        var leaveConfirmHouseholdId by remember<MutableState<String?>> { mutableStateOf(null) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .testTag("household-active-shell"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp,
                    )
                    .widthIn(max = 1200.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Household membership",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    // Add list of households here to make it a "List" component
                    overview.households.forEach { household ->
                        TextButton(onClick = {
                            onHouseholdClick(household.id)
                        }) {
                            Text(text = household.name)
                        }
                    }
                }
            }
        }
        // Leave confirmation dialog
        if (leaveConfirmHouseholdId != null) {
            val householdToLeave = overview.households.find { it.id == leaveConfirmHouseholdId }

            HouseholdLeaveConfirmDialog(
                householdName = householdToLeave?.name ?: leaveConfirmHouseholdId!!,
                isOwner = householdToLeave?.isOwner == true,
                onConfirm = {
                    scope.launch {
                        component.leaveHousehold(leaveConfirmHouseholdId!!)
                        component.loadOverview()
                        leaveConfirmHouseholdId = null
                    }
                },
                onDismiss = { leaveConfirmHouseholdId = null },
            )
        }
    }
}

@Composable
fun HouseholdDetailScreen(
    component: HouseholdDetailComponent,
    modifier: Modifier = Modifier,
) {
    val name by component.householdName.collectAsState()
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Household: $name", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "ID: ${component.householdId}")
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Detailed view coming soon...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun HouseholdSetupContent(
    createJoinComponent: CreateJoinHouseholdComponent,
    activeTab: HouseholdTab,
    scope: kotlinx.coroutines.CoroutineScope,
    width: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
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
                onClick = { createJoinComponent.useCreate() },
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
                onClick = { createJoinComponent.useJoin() },
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
                val state by createJoinComponent.createComponent.uiState.collectAsState()
                CreateHouseholdForm(
                    state = state,
                    onNameChange = createJoinComponent.createComponent::updateHouseholdName,
                    width = width,
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
                    onClick = { scope.launch { createJoinComponent.createComponent.submit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("household-submit"),
                    enabled = !state.isSubmitting,
                ) {
                    Text("Create household")
                }
            }

            HouseholdTab.Join -> {
                val state by createJoinComponent.joinComponent.uiState.collectAsState()
                JoinHouseholdForm(
                    state = state,
                    onCodeChange = createJoinComponent.joinComponent::updateInviteCode,
                    width = width,
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
                    onClick = { scope.launch { createJoinComponent.joinComponent.submit() } },
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
    width: androidx.compose.ui.unit.Dp,
) {
    androidx.compose.material3.OutlinedTextField(
        value = state.householdName,
        onValueChange = onNameChange,
        label = { Text("Household name") },
        isError = state.fieldErrors.containsKey("name"),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = width)
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
    width: androidx.compose.ui.unit.Dp,
) {
    androidx.compose.material3.OutlinedTextField(
        value = state.inviteCode,
        onValueChange = onCodeChange,
        label = { Text("Invite code") },
        isError = state.fieldErrors.containsKey("inviteCode"),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = width)
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
private fun HouseholdLeaveConfirmDialog(
    householdName: String,
    isOwner: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave household?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("You will lose access to this household's shared expenses unless someone invites you again.")
                if (isOwner) {
                    Text(
                        text = "As the owner, leaving will transfer ownership to another member.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("household-leave-confirm"),
            ) {
                Text("Leave household")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("household-leave-cancel"),
            ) {
                Text("Cancel")
            }
        },
        modifier = Modifier.testTag("household-leave-dialog"),
    )
}

@Preview(showBackground = true)
@Composable
fun HouseholdRootScreenPreview() {
    MaterialTheme {
        // Just show a placeholder for preview since FakeHouseholdComponent is gone
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Household Root Screen Preview")
        }
    }
}
