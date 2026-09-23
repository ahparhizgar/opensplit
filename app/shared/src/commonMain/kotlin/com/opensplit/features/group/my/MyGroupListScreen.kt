package com.opensplit.features.group.my

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.opensplit.domain.Group
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.ui.colorSchemeExtended
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun MyGroupsListScreen(
    component: MyGroupsListComponent,
    modifier: Modifier = Modifier,
) {
  val uiState by component.uiState.subscribeAsState()

  Surface(
      modifier = modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background,
  ) {
    val scope = rememberCoroutineScope()
    var leaveConfirmGroupId by rememberSaveable { mutableStateOf<String?>(null) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    val (activeGroups, settledGroups) =
        remember(uiState.groups) {
          uiState.groups.partition {
            !it.isSettled || it.lastInteractionAt >= (Clock.System.now() - 7.days)
          }
        }

    Scaffold(modifier = Modifier.testTag("group-active-shell")) { padding ->
      Column(
          modifier = Modifier.fillMaxSize().padding(padding),
      ) {
        // Top Action Icons
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = { component.onAddGroupClick() }) {
            Icon(
                imageVector = Icons.Default.GroupAdd,
                contentDescription = "Add Group",
                modifier = Modifier.testTag("header-add-group"),
            )
          }
          Box {
            IconButton(onClick = { menuExpanded = true }) {
              Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "More options",
                  modifier = Modifier.testTag("header-more-options"),
              )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
              DropdownMenuItem(
                  text = { Text("Account") },
                  onClick = {
                    menuExpanded = false
                    component.onAccountClick()
                  },
                  modifier = Modifier.testTag("menu-item-account"),
              )
            }
          }
        }

        // Balance Summary Row
        BalanceSummaryRow(
            balance = uiState.overallBalance,
            currency = uiState.overallCurrency,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        )

        val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
        val columns =
            when {
              windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
                  GridCells.Fixed(3)
              windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                  GridCells.Fixed(2)
              else -> GridCells.Fixed(1)
            }

        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier.fillMaxWidth(),
            contentPadding =
                PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Group Cards
          items(activeGroups, key = { it.id }) { group ->
            GroupCard(
                group = group,
                onClick = { component.onGroupClick(group.id) },
                modifier = Modifier.fillMaxWidth().testTag("group-card-${group.id}"),
            )
          }
          // Settled Groups Section
          if (settledGroups.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
              SettledGroupsSection(
                  groups = settledGroups,
                  isExpanded = component.isSettledExpanded.subscribeAsState().value,
                  component = component,
              )
            }
          }
        }
      }
    }

    // Leave confirmation dialog
    if (leaveConfirmGroupId != null) {
      val groupToLeave = uiState.groups.find { it.id == leaveConfirmGroupId }

      GroupLeaveConfirmDialog(
          groupName = groupToLeave?.name ?: leaveConfirmGroupId!!,
          isOwner = groupToLeave?.isOwner == true,
          onConfirm = {
            scope.launch {
              component.leaveGroup(leaveConfirmGroupId!!)
              leaveConfirmGroupId = null
            }
          },
          onDismiss = { leaveConfirmGroupId = null },
      )
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
      val (text, color) =
          when {
            balance > 0 -> "Overall, you are owed " to MaterialTheme.colorScheme.primary
            balance < 0 -> "Overall, you owe " to MaterialTheme.colorScheme.error
            else -> "Overall, you are settled up" to MaterialTheme.colorScheme.onSurfaceVariant
          }
      Text(
          text = text,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Normal,
      )
      if (balance != 0.0) {
        val displayBalance = if (balance < 0) -balance else balance
        Text(
            text = "$currency$displayBalance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
      }
    }
  }
}

@Composable
private fun SettledGroupsSection(
    groups: List<Group>,
    component: MyGroupsListComponent,
    isExpanded: Boolean,
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
          onClick = component::onToggleSettledExpanded,
          modifier = Modifier.testTag("show-settled-btn"),
      ) {
        Text("Show ${groups.size} settled-up groups")
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
        TextButton(onClick = component::onToggleSettledExpanded) {
          Text(
              modifier = Modifier.testTag("hide-settled-btn"),
              text = "Re-hide",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      groups.forEach { group ->
        GroupCard(
            group = group,
            onClick = { component.onGroupClick(group.id) },
        )
      }

      Spacer(Modifier.height(8.dp))

      OutlinedButton(
          onClick = component::onAddGroupClick,
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
private fun GroupCard(
    group: Group,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Surface(
      modifier = modifier,
      onClick = onClick,
      shape = MaterialTheme.shapes.medium,
  ) {
    Row(
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
            text = group.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        val balanceText =
            when {
              group.balance > 0 -> "you are owed IRR${group.balance}"
              group.balance < 0 -> "you owe IRR${-group.balance}"
              else -> "settled up"
            }
        val balanceColor =
            when {
              group.balance > 0 -> MaterialTheme.colorSchemeExtended.youAreOwed.color
              group.balance < 0 -> MaterialTheme.colorSchemeExtended.youOwe.color
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        Text(
            text = balanceText,
            style = MaterialTheme.typography.bodyMedium,
            color = balanceColor,
        )
      }
    }
  }
}

@Composable
private fun GroupLeaveConfirmDialog(
    groupName: String,
    isOwner: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Leave $groupName?") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
              "You will lose access to this group's shared expenses unless someone invites you again."
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
            modifier = Modifier.testTag("group-leave-confirm"),
        ) {
          Text("Leave group")
        }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("group-leave-cancel"),
        ) {
          Text("Cancel")
        }
      },
      modifier = Modifier.testTag("group-leave-dialog"),
  )
}

@Preview
@Composable
private fun MyGroupsListPreview() {
  OpenSplitTheme {
    MyGroupsListScreen(
        component = FakeMyGroupsListComponent(),
    )
  }
}

@Preview
@Composable
private fun MyGroupsListExtendedPreview() {
  OpenSplitTheme {
    MyGroupsListScreen(
        component =
            FakeMyGroupsListComponent(
                isSettledExpanded = MutableValue(true),
            ),
    )
  }
}
