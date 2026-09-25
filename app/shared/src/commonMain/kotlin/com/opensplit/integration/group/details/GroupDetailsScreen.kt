package com.opensplit.integration.group.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.domain.FakeExpenseFactory
import com.opensplit.domain.FakeGroupFactory
import com.opensplit.integration.expense.ExpenseItem
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.ui.components.AdaptiveTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    component: GroupDetailsComponent,
    modifier: Modifier = Modifier,
) {
  val uiState by component.uiState.collectAsState()
  val group = uiState.group
  val currentUserId = group?.members?.find { it.isCurrentUser }?.userId ?: ""

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
      modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        AdaptiveTopAppBar(
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            title = {
              Text(
                  text = group?.name ?: "",
              )
            },
            subtitle = {
              if (group != null) {
                val balanceText =
                    when {
                      group.balance > 0 -> "You are owed IRR${group.balance}"
                      group.balance < 0 -> "You owe IRR${-group.balance}"
                      else -> "Settled up"
                    }
                Text(
                    text = balanceText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            },
            navigationIcon = {
              IconButton(onClick = { component.onBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
              }
            },
            actions = {
              IconButton(onClick = { component.onSettingsClick() }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                )
              }
            },
            scrollBehavior = scrollBehavior,
        )
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { component.onAddExpenseClicked() },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Expense") },
            text = { Text("Expense") },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      },
  ) { padding ->
    Column(modifier = Modifier.padding(padding)) {
      Column {
        if (group != null) {
          if (group.members.size == 1 && uiState.expenses.isEmpty()) {
            Card(modifier = Modifier.padding(16.dp)) {
              Column(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                    text = "You are the only member of this group!",
                )
                Button(
                    onClick = { component.onAddMemberClicked() },
                ) {
                  Row {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(text = "Add Member")
                  }
                }
                FilledTonalButton(
                    onClick = { TODO() },
                ) {
                  Row {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(text = "Share join link")
                  }
                }
              }
            }
          } else {
            LazyColumn(modifier.fillMaxWidth()) {
              items(uiState.expenses) { expense ->
                ExpenseItem(
                    expense = expense,
                    members = group.members,
                    currentUserId = currentUserId,
                    onClick = { component.onExpenseClicked(expense) },
                )
                HorizontalDivider()
              }
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun GroupDetailsScreenLoadingPreview() {
  OpenSplitTheme {
    GroupDetailsScreen(
        component = FakeGroupDetailsComponent(uiState = GroupDetailsComponent.UiState()),
    )
  }
}

@Preview
@Composable
fun GroupDetailsScreenPreview() {
  OpenSplitTheme {
    GroupDetailsScreen(
        component =
            FakeGroupDetailsComponent(
                uiState =
                    GroupDetailsComponent.UiState(
                        group = FakeGroupFactory.create(members = emptyList())
                    )
            ),
    )
  }
}

@Preview
@Composable
fun GroupDetailsScreenWithMemberPreview() {
  OpenSplitTheme {
    GroupDetailsScreen(
        component =
            FakeGroupDetailsComponent(
                uiState =
                    GroupDetailsComponent.UiState(
                        group = FakeGroupFactory.create(),
                        expenses = FakeExpenseFactory.createList(),
                    )
            ),
    )
  }
}
