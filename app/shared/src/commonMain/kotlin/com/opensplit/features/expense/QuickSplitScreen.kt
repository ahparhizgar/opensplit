package com.opensplit.features.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.util.formatAmount

@Composable
fun QuickSplitScreen(component: QuickSplitComponent, modifier: Modifier = Modifier) {
  val uiState by component.uiState.subscribeAsState()
  val amountSum = uiState.amountSum
  val otherName = uiState.other?.name ?: "Other"

  val youOweColor = Color(0xFFE28B52)

  Column(
      modifier = modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    ListItem(
        modifier =
            Modifier.clickable {
              component.onOptionSelected(
                  QuickSplitComponent.QuickSplitOption.YOU_PAID_SPLIT_EQUALLY
              )
            },
        headlineContent = { Text("You paid, split equally.") },
        supportingContent =
            if (amountSum > 0) {
              {
                Text(
                    text = "$otherName owes you IRR ${(amountSum / 2).formatAmount()}",
                    color = MaterialTheme.colorScheme.primary,
                )
              }
            } else null,
        trailingContent = {
          if (
              uiState.selectedOption == QuickSplitComponent.QuickSplitOption.YOU_PAID_SPLIT_EQUALLY
          ) {
            Icon(Icons.Default.Check, contentDescription = null)
          }
        },
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.onOptionSelected(
                  QuickSplitComponent.QuickSplitOption.YOU_ARE_OWED_FULL_AMOUNT
              )
            },
        headlineContent = { Text("You are owed the full amount.") },
        supportingContent =
            if (amountSum > 0) {
              {
                Text(
                    text = "$otherName owes you IRR ${amountSum.formatAmount()}",
                    color = MaterialTheme.colorScheme.primary,
                )
              }
            } else null,
        trailingContent = {
          if (
              uiState.selectedOption ==
                  QuickSplitComponent.QuickSplitOption.YOU_ARE_OWED_FULL_AMOUNT
          ) {
            Icon(Icons.Default.Check, contentDescription = null)
          }
        },
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.onOptionSelected(
                  QuickSplitComponent.QuickSplitOption.OTHER_PAID_SPLIT_EQUALLY
              )
            },
        headlineContent = { Text("$otherName paid, split equally.") },
        supportingContent =
            if (amountSum > 0) {
              {
                Text(
                    text = "You owe $otherName IRR ${(amountSum / 2).formatAmount()}",
                    color = youOweColor,
                )
              }
            } else null,
        trailingContent = {
          if (
              uiState.selectedOption ==
                  QuickSplitComponent.QuickSplitOption.OTHER_PAID_SPLIT_EQUALLY
          ) {
            Icon(Icons.Default.Check, contentDescription = null)
          }
        },
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.onOptionSelected(
                  QuickSplitComponent.QuickSplitOption.OTHER_IS_OWED_FULL_AMOUNT
              )
            },
        headlineContent = { Text("$otherName is owed the full amount.") },
        supportingContent =
            if (amountSum > 0) {
              {
                Text(
                    text = "You owe $otherName IRR ${amountSum.formatAmount()}",
                    color = youOweColor,
                )
              }
            } else null,
        trailingContent = {
          if (
              uiState.selectedOption ==
                  QuickSplitComponent.QuickSplitOption.OTHER_IS_OWED_FULL_AMOUNT
          ) {
            Icon(Icons.Default.Check, contentDescription = null)
          }
        },
    )

    Button(
        onClick = { component.onAdjustSplitClicked() },
        modifier = Modifier.align(Alignment.CenterHorizontally),
    ) {
      Text("More options")
    }
  }
}

@Preview
@Composable
private fun QuickSplitPreview() {
  OpenSplitTheme { QuickSplitScreen(FakeQuickSplitComponent()) }
}
