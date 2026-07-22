package com.opensplit.features.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@Composable
fun PercentageSplitPage(component: PercentageSplitComponent) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(component.initialParticipants) { id ->
        ListItem(
            headlineContent = { Text(id) },
            trailingContent = {
              val style = MaterialTheme.typography.bodyMedium
              TextField(
                  modifier = Modifier.width(100.dp),
                  value = uiState.percentages[id] ?: "",
                  onValueChange = { component.onParticipantPercentageChanged(id, it) },
                  placeholder = { Text(text = "0", style = style) },
                  suffix = { Text("%") },
                  colors = transparentTextFieldColors,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  textStyle = style,
              )
            },
        )
      }
    }
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      val total = uiState.totalPercentage
      val diff = 100.0 - total
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Total: $total%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (kotlin.math.abs(diff) < 0.01) "100% total" else "$diff% left",
            style = MaterialTheme.typography.labelSmall,
            color =
                if (kotlin.math.abs(diff) < 0.01) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PercentageSplitPreview() {
  OpenSplitTheme {
    PercentageSplitPage(
        FakePercentageSplitComponent(
            initialParticipants = listOf("Alice", "Bob"),
            uiState =
                PercentageSplitUiState(
                    percentages = mapOf("Alice" to "50"),
                    totalPercentage = 50.0,
                ),
        )
    )
  }
}
