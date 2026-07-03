package com.opensplit.features.household.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.ui.OpenSplitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdSettingsScreen(component: HouseholdSettingsComponent, modifier: Modifier = Modifier) {
  val uiState by component.uiState.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Group settings") },
            navigationIcon = {
              IconButton(onClick = component::onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
        )
      },
      modifier = modifier,
  ) { paddingValues ->
    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
      if (uiState.isLoading) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      } else if (uiState.error != null) {
        Text(
            text = uiState.error!!,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(Alignment.Center).padding(16.dp),
        )
      } else {
        uiState.household?.let { household ->
          LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(vertical = 16.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            item {
              GroupHeader(modifier = Modifier.padding(horizontal = 16.dp), name = household.name)
            }

            item {
              SectionTitle(modifier = Modifier.padding(horizontal = 16.dp), title = "Group members")
            }

            item {
              SettingsActionItem(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  icon = Icons.Default.PersonAdd,
                  text = "Add people to group",
                  onClick = component::onAddPeopleClicked,
              )
            }

            item {
              val clipboard = LocalClipboardManager.current
              SettingsActionItem(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  icon = Icons.Default.Link,
                  text = "Invite via link",
                  onClick = {
                    clipboard.setText(AnnotatedString(household.inviteLink))
                    component.onInviteLinkClicked()
                  },
              )
            }

            items(household.members) { member ->
              MemberItem(modifier = Modifier.padding(horizontal = 16.dp), member = member)
            }

            item {
              SectionTitle(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  title = "Advanced settings",
              )
            }

            item {
              SettingsActionItem(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  icon = Icons.Default.ExitToApp,
                  text = "Leave group",
                  description =
                      "You can't leave this group because you have outstanding debts with other group members. Please make sure all of your debts have been settled up, and try again.",
                  onClick = component::onLeaveGroupClicked,
              )
            }

            item {
              SettingsActionItem(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  icon = Icons.Default.Delete,
                  text = "Delete group",
                  textColor = MaterialTheme.colorScheme.error,
                  onClick = component::onDeleteGroupClicked,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GroupHeader(name: String, modifier: Modifier = Modifier) {
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Box(
        modifier =
            Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF00ACC1)),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          Icons.Default.FitnessCenter,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(40.dp),
      )
    }
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
  Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      modifier = modifier.padding(vertical = 8.dp),
  )
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    text: String,
    description: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clickable(onClick = onClick)
              .padding(vertical = 12.dp)
              .then(modifier),
      verticalAlignment = if (description != null) Alignment.Top else Alignment.CenterVertically,
  ) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint =
            if (textColor == MaterialTheme.colorScheme.error) textColor
            else MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      Text(text = text, color = textColor, style = MaterialTheme.typography.bodyLarge)
      if (description != null) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun MemberItem(member: HouseholdMemberDto, modifier: Modifier = Modifier) {
  Row(
      modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
        modifier =
            Modifier.size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
      Icon(Icons.Default.Person, contentDescription = null)
    }
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
          text = member.name ?: member.email,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
      )
      Text(
          text = member.email,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Column(horizontalAlignment = Alignment.End) {
      val balanceText = if (member.balance >= 0) "gets back" else "owes"
      val balanceColor = if (member.balance >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63)

      Text(
          text = balanceText,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
          text = "${member.balanceCurrency}${kotlin.math.abs(member.balance)}",
          style = MaterialTheme.typography.bodyMedium,
          color = balanceColor,
          fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HouseholdSettingsScreenPreview() {
  OpenSplitTheme { HouseholdSettingsScreen(FakeHouseholdSettingsComponent()) }
}
