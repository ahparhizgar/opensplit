package com.opensplit.features.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@Composable
fun QuickSplitScreen(component: QuickSplitComponent, modifier: Modifier = Modifier) {
  val uiState by component.uiState.subscribeAsState()
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
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.onOptionSelected(
                  QuickSplitComponent.QuickSplitOption.YOU_ARE_OWED_FULL_AMOUNT
              )
            },
        headlineContent = { Text("You are owed the full amount.") },
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.onOptionSelected(
                  QuickSplitComponent.QuickSplitOption.OTHER_PAID_SPLIT_EQUALLY
              )
            },
        headlineContent = { Text("${uiState.other?.name} paid, split equally.") },
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.onOptionSelected(
                  QuickSplitComponent.QuickSplitOption.OTHER_IS_OWED_FULL_AMOUNT
              )
            },
        headlineContent = { Text("${uiState.other?.name} is owed the full amount.") },
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
