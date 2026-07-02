package com.opensplit.features.household.my

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.ui.components.BottomNav
import kotlinx.coroutines.launch

@Composable
fun MyHouseholdsListScreen(
    component: MyHouseholdsListComponent,
    onHouseholdClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by component.isLoading.collectAsState()
    val overview by component.overview.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val scope = rememberCoroutineScope()
            var leaveConfirmHouseholdId by remember<MutableState<String?>> { mutableStateOf(null) }
            var selectedNavIndex by remember { mutableStateOf(0) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("household-active-shell"),
            ) {
                // Main scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "OpenSplit",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.testTag("header-title"),
                        )
                        Row(
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { }
                                    .testTag("header-search"),
                            )
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Household",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { component.onAddHouseholdClick() }
                                    .testTag("header-profile"),
                            )
                        }
                    }

                    // Main content in LazyColumn
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Balance Summary
                        item {
                            BalanceSummaryCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                            )
                        }

                        // Household Cards
                        items(overview.households) { household ->
                            HouseholdCard(
                                household = household,
                                onClick = { onHouseholdClick(household.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .testTag("household-card-${household.id}"),
                            )
                        }

                        // FAB - Add Expense (styled as a pill button)
                        item {
                            AddExpenseFab(
                                onClick = { /* Navigate to add expense */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                            )
                        }
                    }
                }

                BottomNav(
                    selectedIndex = selectedNavIndex,
                    onItemSelected = { selectedNavIndex = it },
                    modifier = Modifier.fillMaxWidth(),
                )
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
}

@Composable
private fun BalanceSummaryCard(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Overall, you are owed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$42.50",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Filter",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable { }
                .testTag("balance-filter"),
        )
    }
}

@Composable
private fun HouseholdCard(
    household: HouseholdSummaryDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val statusColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = statusColor,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = household.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = household.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (household.isOwner) "Owner" else "Member",
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
            )
            if (household.memberCount > 0) {
                Text(
                    text = "${household.memberCount} ${if (household.memberCount == 1) "member" else "members"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddExpenseFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(vertical = 8.dp)
            .testTag("add-expense-fab"),
    ) {
        Text(
            text = "+ Add Expense",
            style = MaterialTheme.typography.labelLarge,
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
        title = { Text("Leave $householdName?") },
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

@Preview
@Composable
private fun MyHouseholdsListPreview() {
    OpenSplitTheme {
        MyHouseholdsListScreen(
            component = FakeMyHouseholdsListComponent(),
            onHouseholdClick = {},
        )
    }
}
