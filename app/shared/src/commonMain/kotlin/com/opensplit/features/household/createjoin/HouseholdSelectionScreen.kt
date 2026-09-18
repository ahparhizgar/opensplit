package com.opensplit.features.household.createjoin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.ui.OpenSplitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdSelectionScreen(
    component: HouseholdSelectionComponent,
    modifier: Modifier = Modifier,
) {
  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = { Text("Household") },
            navigationIcon = {
              IconButton(onClick = { component.onBackClicked() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            }
        )
      }
  ) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Surface(
          modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
            component.onCreateHouseholdClicked()
          }.testTag("household-create-btn"),
          color = MaterialTheme.colorScheme.surfaceVariant,
      ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
              tint = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
                text = "Create a Household",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Set up a new space to split expenses with your roommates.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Surface(
          modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
            component.onJoinHouseholdClicked()
          }.testTag("household-join-btn"),
          color = MaterialTheme.colorScheme.surfaceVariant,
      ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
              imageVector = Icons.Default.Link,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
              tint = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
                text = "Join a Group",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Got an invite link? Join your existing friends here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun HouseholdSelectionScreenPreview() {
  OpenSplitTheme {
    HouseholdSelectionScreen(FakeHouseholdSelectionComponent())
  }
}
