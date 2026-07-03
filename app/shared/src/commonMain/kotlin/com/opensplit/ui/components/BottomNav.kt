package com.opensplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.ui.OpenSplitTheme

/**
 * Bottom navigation bar with 4 project-specific tabs: Households, Friends, Activity, Account. Fixed
 * at the bottom of screens with Material 3 styling.
 *
 * @param selectedIndex Index of the currently selected tab (0-3)
 * @param onItemSelected Callback when a tab is clicked, receives tab index (0-3)
 * @param modifier Optional modifier for the container
 */
@Composable
fun BottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier.background(MaterialTheme.colorScheme.surface).testTag("bottom-nav"),
  ) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.surfaceVariant,
        thickness = 1.dp,
    )

    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    ) {
      BottomNavItem(
          label = "Households",
          icon = Icons.Default.Home,
          isSelected = 0 == selectedIndex,
          onClick = { onItemSelected(0) },
          modifier = Modifier.weight(1f).testTag("bottom-nav-item-households"),
      )
      BottomNavItem(
          label = "Friends",
          icon = Icons.Default.AccountBox,
          isSelected = 1 == selectedIndex,
          onClick = { onItemSelected(1) },
          modifier = Modifier.weight(1f).testTag("bottom-nav-item-friends"),
      )
      BottomNavItem(
          label = "Activity",
          icon = Icons.Default.Schedule,
          isSelected = 2 == selectedIndex,
          onClick = { onItemSelected(2) },
          modifier = Modifier.weight(1f).testTag("bottom-nav-item-activity"),
      )
      BottomNavItem(
          label = "Account",
          icon = Icons.Default.AccountBox,
          isSelected = 3 == selectedIndex,
          onClick = { onItemSelected(3) },
          modifier = Modifier.weight(1f).testTag("bottom-nav-item-account"),
      )
    }
  }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val contentColor =
      if (isSelected) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant
      }

  Column(
      modifier =
          modifier
              .clickable(onClick = onClick)
              .background(MaterialTheme.colorScheme.surface)
              .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(24.dp),
    )

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
    )
  }
}

@Preview
@Composable
private fun BottomNavPreview() {
  OpenSplitTheme {
    BottomNav(
        selectedIndex = 0,
        onItemSelected = {},
    )
  }
}
