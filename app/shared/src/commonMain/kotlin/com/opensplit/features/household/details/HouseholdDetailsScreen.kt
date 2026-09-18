package com.opensplit.features.household.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.domain.FakeExpenseFactory
import com.opensplit.domain.FakeHouseholdFactory
import com.opensplit.features.expense.ExpenseItem
import com.opensplit.ui.OpenSplitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdDetailsScreen(
    component: HouseholdDetailsComponent,
    modifier: Modifier = Modifier,
) {
  val uiState by component.uiState.collectAsState()
  val household = uiState.household
  val currentUserId = household?.members?.find { it.isCurrentUser }?.userId ?: ""

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
      modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        LargeTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            title = {
              Column {
                Text(
                    text = household?.name ?: "",
                    style = MaterialTheme.typography.displaySmall,
                )
                if (household != null) {
                  val balanceText =
                      when {
                        household.balance > 0 -> "You are owed IRR${household.balance}"
                        household.balance < 0 -> "You owe IRR${-household.balance}"
                        else -> "Settled up"
                      }
                  Text(
                      text = balanceText,
                      style = MaterialTheme.typography.titleMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
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
            scrollBehavior = scrollBehavior
        )
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { component.onAddExpenseClicked() },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Expense") },
            text = { Text("Expense") },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
  ) { padding ->
    Column(modifier = Modifier.padding(padding)) {
      Column {
        if (household != null) {
          if (household.members.isEmpty()) {
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
                    members = household.members,
                    currentUserId = currentUserId,
                    onClick = { component.onExpenseClicked(expense) },
                )
                androidx.compose.material3.HorizontalDivider()
              }
            }
          }
        }
      }
    }
  }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdDetailsScreenLoadingPreview() {
  OpenSplitTheme {
    HouseholdDetailsScreen(
        component = FakeHouseholdDetailsComponent(uiState = HouseholdDetailsComponent.UiState()),
    )
  }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdDetailsScreenPreview() {
  OpenSplitTheme {
    HouseholdDetailsScreen(
        component =
            FakeHouseholdDetailsComponent(
                uiState =
                    HouseholdDetailsComponent.UiState(
                        household = FakeHouseholdFactory.create(members = emptyList())
                    )
            ),
    )
  }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdDetailsScreenWithMemberPreview() {
  OpenSplitTheme {
    HouseholdDetailsScreen(
        component =
            FakeHouseholdDetailsComponent(
                uiState =
                    HouseholdDetailsComponent.UiState(
                        household = FakeHouseholdFactory.create(),
                        expenses = FakeExpenseFactory.createList(),
                    )
            ),
    )
  }
}
