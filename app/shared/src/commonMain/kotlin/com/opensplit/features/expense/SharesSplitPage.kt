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
fun SharesSplitPage(component: SharesSplitComponent) {
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
                  value = uiState.shares[id] ?: "",
                  onValueChange = { component.onParticipantSharesChanged(id, it) },
                  placeholder = { Text(text = "0", style = style) },
                  colors = transparentTextFieldColors,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  textStyle = style,
              )
            },
        )
      }
    }
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Total shares: ${uiState.totalShares}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Preview
@Composable
private fun SharesSplitPreview() {
  OpenSplitTheme {
    SharesSplitPage(
        FakeSharesSplitComponent(
            initialParticipants = listOf("Alice", "Bob"),
            uiState =
                SharesSplitUiState(shares = mapOf("Alice" to "2", "Bob" to "1"), totalShares = 3),
        )
    )
  }
}
