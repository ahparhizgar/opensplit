package com.opensplit.features.household.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.opensplit.ui.OpenSplitTheme
import kotlinx.coroutines.launch

@Composable
fun MyHouseholdsListScreen(
    component: MyHouseholdsListComponent,
    onHouseholdClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by component.isLoading.collectAsState()
    val overview by component.overview.collectAsState()
    Surface(modifier = modifier) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
