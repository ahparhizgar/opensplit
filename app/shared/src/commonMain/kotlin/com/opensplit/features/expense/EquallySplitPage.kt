package com.opensplit.features.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@Composable
fun EquallySplitPage(component: EquallySplitComponent, onDone: () -> Unit = {}) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier =
            Modifier.weight(1f).onKeyEvent {
              if (it.isCmdEnter()) {
                onDone()
                true
              } else {
                false
              }
            }
    ) {
      items(component.initialParticipants) { id ->
        ListItem(
            headlineContent = { Text(id) },
            trailingContent = {
              Checkbox(
                  checked = id in uiState.userIds,
                  onCheckedChange = { component.onParticipantInclusionChanged(id, it) },
              )
            },
        )
      }
    }
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      val includedCount = uiState.userIds.size
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Split equally among $includedCount people",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Preview
@Composable
private fun EquallySplitPreview() {
  OpenSplitTheme {
    EquallySplitPage(
        FakeEquallySplitComponent(
            initialParticipants = listOf("Alice", "Bob", "Charlie"),
            uiState = EquallySplitUiState(userIds = setOf("Alice", "Bob")),
        )
    )
  }
}
