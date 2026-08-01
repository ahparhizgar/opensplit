package com.opensplit.features.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@Composable
fun WhoPaidScreen(component: WhoPaidComponent, modifier: Modifier = Modifier) {
  val uiState by component.uiState.subscribeAsState()
  LazyColumn(modifier = modifier.fillMaxSize()) {
    items(uiState.participants) { participant ->
      val isPayer = uiState.selectedUserId == participant.userId
      ListItem(
          leadingContent = {
            Box(
                modifier =
                    Modifier.size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
              Icon(Icons.Default.Person, contentDescription = null)
            }
          },
          headlineContent = { Text(if (participant.isCurrentUser) "you" else participant.name) },
          trailingContent = { if (isPayer) Icon(Icons.Default.Check, contentDescription = null) },
          modifier = Modifier.clickable { component.onParticipantSelected(participant.userId) },
      )
    }
    item {
      ListItem(
          headlineContent = {
            Text(
                "Multiple people",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
          },
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
