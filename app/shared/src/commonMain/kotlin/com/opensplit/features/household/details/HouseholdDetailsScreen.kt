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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.dto.household.FakeHouseholdDtoFactory
import com.opensplit.ui.OpenSplitTheme

@Composable
fun HouseholdDetailsScreen(
    component: HouseholdDetailsComponent,
    modifier: Modifier = Modifier,
) {
  val uiState by component.uiState.collectAsState()
  val household = uiState.household
  Scaffold {
    Column {
      Box(
          modifier =
              Modifier.background(MaterialTheme.colorScheme.primary)
                  .height(240.dp)
                  .fillMaxWidth()
                  .padding(16.dp)
      ) {
        IconButton(onClick = { component.onBack() }) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onPrimary,
          )
        }

        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { component.onSettingsClick() },
        ) {
          Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = MaterialTheme.colorScheme.onPrimary,
          )
        }
        if (household != null) {
          Text(
              modifier = Modifier.align(Alignment.BottomStart),
              text = household.name,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onPrimary,
          )
        }
      }

      Spacer(Modifier.height(16.dp))

      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (household != null) {
          if (household.members.isEmpty()) {
            Card {
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
              // TODO show transactions
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun HouseholdDetailsScreenLoadingPreview() {
  OpenSplitTheme {
    HouseholdDetailsScreen(
        component = FakeHouseholdDetailsComponent(uiState = HouseholdDetailsComponent.UiState()),
    )
  }
}

@Preview
@Composable
fun HouseholdDetailsScreenPreview() {
  OpenSplitTheme {
    HouseholdDetailsScreen(
        component =
            FakeHouseholdDetailsComponent(
                uiState =
                    HouseholdDetailsComponent.UiState(
                        household = FakeHouseholdDtoFactory.create(members = emptyList())
                    )
            ),
    )
  }
}

@Preview
@Composable
fun HouseholdDetailsScreenWithMemberPreview() {
  OpenSplitTheme {
    HouseholdDetailsScreen(
        component =
            FakeHouseholdDetailsComponent(
                uiState =
                    HouseholdDetailsComponent.UiState(household = FakeHouseholdDtoFactory.create())
            ),
    )
  }
}
