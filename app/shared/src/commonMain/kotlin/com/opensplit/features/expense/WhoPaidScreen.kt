package com.opensplit.features.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@Composable
fun WhoPaidScreen(component: WhoPaidComponent, modifier: Modifier = Modifier) {
  val uiState by component.uiState.subscribeAsState()
  LazyColumn(modifier = modifier.fillMaxSize()) {
    items(uiState.allParticipants) { participant ->
      val isPayer = uiState.selectedUserId == participant
      ListItem(
          // Todo: convert to name
          headlineContent = { Text(participant) },
          trailingContent = { if (isPayer) Icon(Icons.Default.Check, contentDescription = null) },
          modifier = Modifier.clickable { component.onParticipantSelected(participant) },
      )
    }
    item {
      ListItem(
          headlineContent = { Text("Multiple people") },
          modifier = Modifier.clickable { component.onMultiplePeopleClicked() },
      )
    }
  }
}

@Preview
@Composable
private fun WhoPaidPreview() {
  OpenSplitTheme { WhoPaidScreen(FakeWhoPaidComponent()) }
}
