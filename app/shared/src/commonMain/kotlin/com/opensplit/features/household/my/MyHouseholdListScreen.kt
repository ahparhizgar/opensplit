package com.opensplit.features.household.my

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.ui.components.BottomNav
import kotlinx.coroutines.launch

@Composable
fun MyHouseholdsListScreen(
    component: MyHouseholdsListComponent,
    modifier: Modifier = Modifier,
) {
  val isLoading by component.isLoading.subscribeAsState()
  val overview by component.overview.subscribeAsState()

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
      var leaveConfirmHouseholdId by rememberSaveable { mutableStateOf<String?>(null) }
      var selectedNavIndex by rememberSaveable { mutableStateOf(0) }

      val (activeHouseholds, settledHouseholds) =
          remember(overview.households) { overview.households.partition { !it.isSettled } }

      Scaffold(
          bottomBar = {
            BottomNav(
                selectedIndex = selectedNavIndex,
                onItemSelected = { selectedNavIndex = it },
                modifier = Modifier.fillMaxWidth(),
            )
          },
          floatingActionButton = { AddExpenseFab(onClick = { /* Navigate to add expense */ }) },
          modifier = Modifier.testTag("household-active-shell"),
      ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
          // Top Action Icons
          Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(onClick = {}) {
              Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  modifier = Modifier.testTag("header-search"),
              )
            }
            IconButton(onClick = { component.onAddHouseholdClick() }) {
              Icon(
                  imageVector = Icons.Default.GroupAdd,
                  contentDescription = "Add Household",
                  modifier = Modifier.testTag("header-add-group"),
              )
            }
          }

          // Balance Summary Row
          BalanceSummaryRow(
              balance = overview.overallBalance,
              currency = overview.overallCurrency,
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
          )

          LazyColumn(
              modifier = Modifier.fillMaxWidth(),
              contentPadding =
                  androidx.compose.foundation.layout.PaddingValues(
                      horizontal = 16.dp,
                      vertical = 8.dp,
                  ),
              verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            // Household Cards
            items(activeHouseholds) { household ->
              HouseholdCard(
                  household = household,
                  onClick = { component.onHouseholdClick(household.id) },
                  modifier = Modifier.fillMaxWidth().testTag("household-card-${household.id}"),
              )
            }

            // Sample "Non-group expenses" for visual fidelity
            item { NonGroupExpensesCard() }

            // Settled Groups Section
            if (settledHouseholds.isNotEmpty()) {
              item {
                SettledGroupsSection(
                    households = settledHouseholds,
                    isExpanded = component.isSettledExpanded.subscribeAsState().value,
                    onToggle = { component.onToggleSettledExpanded() },
                    onStartNewGroup = { component.onAddHouseholdClick() },
                )
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
private fun BalanceSummaryRow(
    balance: Double,
    currency: String,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier,
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
          text = "Overall, you are owed ",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Normal,
      )
      Text(
          text = "$currency$balance",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
      )
    }
    Icon(
        imageVector = Icons.Default.Tune,
        contentDescription = "Filter",
        modifier = Modifier.size(28.dp).clickable {}.testTag("balance-filter"),
    )
  }
}

@Composable
private fun NonGroupExpensesCard() {
  Row(
      modifier = Modifier.fillMaxWidth().clickable {}.padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
        modifier =
            Modifier.size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
      Box(
          modifier = Modifier.size(32.dp).background(Color.White, CircleShape),
      )
    }

    Column {
      Text(
          text = "Non-group expenses",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
      )
      Text(
          text = "settled up",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun SettledGroupsSection(
    households: List<HouseholdSummaryDto>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onStartNewGroup: () -> Unit,
) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    if (!isExpanded) {
      Text(
          text = "Hiding groups that have been settled up over one month.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      OutlinedButton(
          onClick = onToggle,
          modifier = Modifier.testTag("show-settled-btn"),
      ) {
        Text("Show ${households.size} settled-up groups")
      }
    } else {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "Previously settled groups. ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onToggle) {
          Text(
              text = "Re-hide",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.clickable(onClick = onToggle).testTag("hide-settled-btn"),
          )
        }
      }

      households.forEach { household -> SettledHouseholdCard(name = household.name) }

      Spacer(Modifier.height(8.dp))

      OutlinedButton(
          onClick = onStartNewGroup,
          modifier = Modifier.wrapContentWidth().testTag("start-new-group-btn"),
      ) {
        Icon(
            imageVector = Icons.Default.GroupAdd,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text("Start a new group")
      }
    }
  }
}

@Composable
private fun SettledHouseholdCard(name: String) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
        modifier =
            Modifier.size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                )
    )
    Column {
      Text(
          text = name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
      )
      Text(
          text = "settled up",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
      )
    }
  }
}

@Composable
private fun HouseholdCard(
    household: HouseholdSummaryDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
        modifier =
            Modifier.size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = Icons.Default.Groups,
          contentDescription = null,
          modifier = Modifier.size(32.dp),
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
          text = household.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
      )
      Text(
          text = "you are owed ${household.currency}${household.balance}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary,
      )
      household.description?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun AddExpenseFab(
    onClick: () -> Unit,
) {
  ExtendedFloatingActionButton(
      onClick = onClick,
      icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
      text = { Text("Add expense") },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier.testTag("add-expense-fab"),
  )
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
          Text(
              "You will lose access to this household's shared expenses unless someone invites you again."
          )
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
    )
  }
}

@Preview
@Composable
private fun MyHouseholdsListExtendedPreview() {
  OpenSplitTheme {
    MyHouseholdsListScreen(
        component =
            FakeMyHouseholdsListComponent(
                isSettledExpanded = MutableValue(true),
            ),
    )
  }
}
