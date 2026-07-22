package com.opensplit.features.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@Composable
fun AdjustmentSplitPage(component: AdjustmentSplitComponent) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(component.initialParticipants) { id ->
        ListItem(
            headlineContent = { Text(id) },
            supportingContent = { Text("IRR ${uiState.adjustments[id] ?: "0.00"}") },
            trailingContent = {
              val style = MaterialTheme.typography.bodyMedium
              TextField(
                  modifier = Modifier.width(120.dp),
                  value = uiState.adjustments[id] ?: "",
                  onValueChange = { component.onParticipantAdjustmentChanged(id, it) },
                  placeholder = { Text(text = "0.00", style = style) },
                  prefix = { Text("+ IRR ") },
                  colors = transparentTextFieldColors,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  textStyle = style,
              )
            },
        )
      }
    }
  }
}

@Preview
@Composable
private fun AdjustmentSplitPreview() {
  OpenSplitTheme {
    AdjustmentSplitPage(
        FakeAdjustmentSplitComponent(
            initialParticipants = listOf("Alice", "Bob"),
            uiState = AdjustmentSplitUiState(adjustments = mapOf("Alice" to "100.00")),
        )
    )
  }
}
