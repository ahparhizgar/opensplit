package com.opensplit.features.household

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.opensplit.ui.AdaptiveLayoutSizeClass
import com.opensplit.ui.adaptiveLayoutSizeClass

@Composable
fun HouseholdRootScreen(
    component: HouseholdComponent,
    modifier: Modifier = Modifier,
) {
    // Trigger the initial overview load the first time this screen is composed.
    // This lets the component remain a pure state-holder while the UI drives
    // the async fetch, giving us a clean loading → route decision.
    LaunchedEffect(component) {
        component.loadOverview()
    }

    val isLoading by component.isLoadingOverview.collectAsState()
    val householdId by component.householdId.collectAsState()
    val overview by component.overview.collectAsState()
    Surface(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val sizeClass = adaptiveLayoutSizeClass(maxWidth)
            when {
                isLoading -> {
                    // Show a centered spinner while checking whether the user
                    // already belongs to a household.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("household-loading"),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                householdId == null -> {
                    HouseholdSetupScreen(component = component, sizeClass = sizeClass)
                }
                else -> {
                    HouseholdActiveScreen(
                        householdId = householdId!!,
                        overview = overview,
                        onSwitchHousehold = component::switchHousehold,
                        onLeaveHousehold = component::leaveHousehold,
                        onRefresh = component::loadOverview,
                        sizeClass = sizeClass,
                    )
                }
            }
        }
    }
}

@Composable
fun HouseholdSetupScreen(
    component: HouseholdComponent,
    sizeClass: AdaptiveLayoutSizeClass,
) {
    val activeTab by component.activeTab.collectAsState()
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp)
            .testTag("household-setup-shell"),
        contentAlignment = Alignment.Center,
    ) {
        when (sizeClass) {
            AdaptiveLayoutSizeClass.Expanded -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1120.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HouseholdSetupHeroPanel(
                        modifier = Modifier.weight(1f),
                    )
                    HouseholdSetupContent(
                        component = component,
                        activeTab = activeTab,
                        scope = scope,
                        width = 420.dp,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 420.dp),
                    )
                }
            }

            AdaptiveLayoutSizeClass.Medium -> HouseholdSetupContent(
                component = component,
                activeTab = activeTab,
                scope = scope,
                width = 560.dp,
                modifier = Modifier.widthIn(max = 560.dp),
            )

            AdaptiveLayoutSizeClass.Compact -> HouseholdSetupContent(
                component = component,
                activeTab = activeTab,
                scope = scope,
                width = 420.dp,
                modifier = Modifier.widthIn(max = 420.dp),
            )
        }
    }
}

@Composable
private fun HouseholdSetupHeroPanel(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = "Set up your household", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Create a new household or join one with an invite code. On wider screens, the action form stays visible while context stays nearby.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "• Create a fresh household")
            Text(text = "• Join with an invite code or household ID")
            Text(text = "• Keep the current household context clear")
        }
    }
}

@Composable
private fun HouseholdSetupContent(
    component: HouseholdComponent,
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
fun HouseholdActiveScreen(
    householdId: String,
    overview: com.opensplit.dto.household.HouseholdOverviewResponse,
    onSwitchHousehold: suspend (String) -> Unit,
    onLeaveHousehold: suspend (String) -> Unit,
    onRefresh: suspend () -> Unit,
    sizeClass: AdaptiveLayoutSizeClass,
) {
    val scope = rememberCoroutineScope()
    val activeHousehold = overview.households.find { it.isActive }
    var leaveConfirmHouseholdId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .testTag("household-active-shell"),
    ) {
        // Header bar for desktop
        if (sizeClass.isExpanded) {
            HouseholdDesktopHeader(
                activeHouseholdName = activeHousehold?.name ?: householdId,
                onRefresh = onRefresh,
                scope = scope,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (sizeClass.isExpanded) 64.dp else 24.dp,
                    vertical = if (sizeClass.isExpanded) 24.dp else 16.dp,
                )
                .widthIn(max = 1200.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (!sizeClass.isExpanded) {
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
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = "Active: ${activeHousehold?.name ?: householdId}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .testTag("household-context-mobile")
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            if (sizeClass.isExpanded) {
                // Desktop two-column layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    // Left column (7 of 12): membership context
                    Column(
                        modifier = Modifier.weight(0.583f), // 7/12
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HouseholdSummaryCard(
                            household = activeHousehold,
                            householdId = householdId,
                        )
                        HouseholdMembersCard(
                            overview = overview,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HouseholdSafetyNote()
                    }

                    // Right column (5 of 12): household actions
                    Column(
                        modifier = Modifier.weight(0.417f), // 5/12
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HouseholdSwitchCard(
                            overview = overview,
                            onSwitchHousehold = { householdId ->
                                scope.launch {
                                    onSwitchHousehold(householdId)
                                    onRefresh()
                                }
                            },
                            onLeaveClick = { householdId -> leaveConfirmHouseholdId = householdId },
                            scope = scope,
                            isDesktop = true,
                        )
                    }
                }
            } else {
                // Mobile/tablet single column layout
                HouseholdSummaryCard(
                    household = activeHousehold,
                    householdId = householdId,
                )
                HouseholdMembersCard(
                    overview = overview,
                    modifier = Modifier.fillMaxWidth(),
                )
                HouseholdSwitchCard(
                    overview = overview,
                    onSwitchHousehold = { householdId ->
                        scope.launch {
                            onSwitchHousehold(householdId)
                            onRefresh()
                        }
                    },
                    onLeaveClick = { householdId -> leaveConfirmHouseholdId = householdId },
                    scope = scope,
                    isDesktop = false,
                )
                HouseholdSafetyNote()
            }
        }
    }

    // Leave confirmation dialog
    if (leaveConfirmHouseholdId != null) {
        val householdToLeave = overview.households.find { it.id == leaveConfirmHouseholdId }
        val activeHouseholdId = overview.activeHouseholdId
        val isLeavingActive = leaveConfirmHouseholdId == activeHouseholdId
        val hasAlternativeHouseholds = overview.households.any { !it.isActive }

        HouseholdLeaveConfirmDialog(
            householdName = householdToLeave?.name ?: leaveConfirmHouseholdId!!,
            isActive = isLeavingActive,
            hasAlternatives = hasAlternativeHouseholds,
            onConfirm = {
                scope.launch {
                    onLeaveHousehold(leaveConfirmHouseholdId!!)
                    onRefresh()
                    leaveConfirmHouseholdId = null
                }
            },
            onDismiss = { leaveConfirmHouseholdId = null },
        )
    }
}

@Composable
private fun HouseholdDesktopHeader(
    activeHouseholdName: String,
    onRefresh: suspend () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .testTag("household-desktop-header"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "OpenSplit",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Household membership",
                    style = MaterialTheme.typography.titleLarge,
                )
                // Active household pill
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text(
                        text = "Active: $activeHouseholdName",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            // Utility actions
            TextButton(
                onClick = { scope.launch { onRefresh() } },
                modifier = Modifier.testTag("household-refresh"),
            ) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun HouseholdSummaryCard(
    household: com.opensplit.dto.household.HouseholdSummaryResponse?,
    householdId: String,
) {
    val inviteCode = household?.inviteCode
    var copied by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("household-summary-card"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = household?.name ?: householdId,
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Active household",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.testTag("household-active-label"),
                )
                Text(
                    text = "${household?.memberCount ?: 0} members",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (household?.isOwner == true) {
                    Text(
                        text = "You are owner",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (inviteCode != null) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Invite code:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.testTag("household-invite-code-label"),
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = inviteCode,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            ),
                            modifier = Modifier
                                .testTag("household-invite-code-value")
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    TextButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(inviteCode))
                            copied = true
                        },
                        modifier = Modifier.testTag("household-copy-invite-code"),
                    ) {
                        Text(if (copied) "Copied!" else "Copy")
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseholdSafetyNote() {
    Text(
        text = "Switching households changes your current context, not your membership in other households.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .testTag("household-safety-note"),
    )
}

@Composable
private fun HouseholdMembersCard(
    overview: com.opensplit.dto.household.HouseholdOverviewResponse,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(),
        modifier = modifier.testTag("household-members-card"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Members",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Everyone who can currently share expenses in this household",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (overview.members.isEmpty()) {
                Text(
                    text = "No members found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(overview.members, key = { it.userId }) { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = member.email,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("household-member-${member.userId}"),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(start = 8.dp),
                            ) {
                                if (member.isCurrentUser) {
                                    Text(
                                        text = "You",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                                if (member.isOwner) {
                                    Text(
                                        text = "Owner",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseholdSwitchCard(
    overview: com.opensplit.dto.household.HouseholdOverviewResponse,
    onSwitchHousehold: suspend (String) -> Unit,
    onLeaveClick: (String) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    isDesktop: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("household-switch-card"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Your households",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Change which household you are currently viewing",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(if (isDesktop) 12.dp else 8.dp),
                modifier = Modifier.heightIn(max = if (isDesktop) 400.dp else 220.dp),
            ) {
                items(overview.households, key = { it.id }) { household ->
                    HouseholdRow(
                        household = household,
                        onSwitchHousehold = { scope.launch { onSwitchHousehold(it) } },
                        onLeaveClick = { onLeaveClick(household.id) },
                        isDesktop = isDesktop,
                    )
                }
            }

            if (overview.households.size == 1) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You only belong to one household right now",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HouseholdRow(
    household: com.opensplit.dto.household.HouseholdSummaryResponse,
    onSwitchHousehold: (String) -> Unit,
    onLeaveClick: () -> Unit,
    isDesktop: Boolean,
) {
    Surface(
        color = if (household.isActive)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("household-row-${household.id}"),
    ) {
        if (isDesktop) {
            // Desktop layout: horizontal row with info on left, actions on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = household.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "${household.memberCount} members",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { onSwitchHousehold(household.id) },
                        enabled = !household.isActive,
                        modifier = Modifier.testTag("household-switch-${household.id}"),
                    ) {
                        Text(if (household.isActive) "Current" else "Switch")
                    }
                    OutlinedButton(
                        onClick = onLeaveClick,
                        modifier = Modifier.testTag("household-leave-${household.id}"),
                    ) {
                        Text("Leave")
                    }
                }
            }
        } else {
            // Mobile layout: stacked with full-width buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = household.name,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "${household.memberCount} members",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { onSwitchHousehold(household.id) },
                        enabled = !household.isActive,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("household-switch-${household.id}"),
                    ) {
                        Text(if (household.isActive) "Current" else "Switch")
                    }
                    OutlinedButton(
                        onClick = onLeaveClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("household-leave-${household.id}"),
                    ) {
                        Text("Leave")
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseholdLeaveConfirmDialog(
    householdName: String,
    isActive: Boolean,
    hasAlternatives: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave household?") },
        text = {
            Text(
                when {
                    !isActive -> "You will lose access to this household's shared expenses unless someone invites you again."
                    hasAlternatives -> "You will leave $householdName and switch to another household you still belong to."
                    else -> "You will leave $householdName and return to household setup."
                }
            )
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

@Composable
private fun HouseholdHouseholdsCard(
    overview: com.opensplit.dto.household.HouseholdOverviewResponse,
    onSwitchHousehold: suspend (String) -> Unit,
    onLeaveHousehold: suspend (String) -> Unit,
    onRefresh: suspend () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    listMaxHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Card(colors = CardDefaults.cardColors(), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Households", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = listMaxHeight),
            ) {
                items(overview.households, key = { it.id }) { household ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (household.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = household.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${household.memberCount} members")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { scope.launch { onSwitchHousehold(household.id); onRefresh() } },
                                    enabled = !household.isActive,
                                    modifier = Modifier.testTag("household-switch-${household.id}"),
                                ) { Text(if (household.isActive) "Current" else "Switch") }
                                Button(
                                    onClick = { scope.launch { onLeaveHousehold(household.id); onRefresh() } },
                                    modifier = Modifier.testTag("household-leave-${household.id}"),
                                ) { Text("Leave") }
                            }
                        }
                    }
                }
            }
        }
    }
}

