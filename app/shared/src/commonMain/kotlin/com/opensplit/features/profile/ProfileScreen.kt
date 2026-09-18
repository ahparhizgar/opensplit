package com.opensplit.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    component: ProfileComponent,
    modifier: Modifier = Modifier,
) {
  val uiState by component.uiState.subscribeAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Account",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
              )
            },
            navigationIcon = {
              IconButton(onClick = component::onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
              }
            },
        )
      },
      modifier = modifier,
  ) { paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
    ) {
      ProfileHeader(
          component = component,
          name = uiState.name,
          email = uiState.email,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      )

      Spacer(modifier = Modifier.height(16.dp))

      LogoutButton(
          component = component,
      )
    }
  }
}

@Composable
private fun ProfileHeader(
    component: ProfileComponent,
    name: String,
    email: String,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    AvatarWithCamera()

    Spacer(modifier = Modifier.width(16.dp))

    Column(
        modifier = Modifier.weight(1f),
    ) {
      Text(
          text = name.ifEmpty { "AmirHossein" },
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
          text = email.ifEmpty { "amparhizgar@gmail.com" },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    TextButton(
        onClick = component::onEditClicked,
    ) {
      Text(
          text = "Edit",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

@Composable
private fun AvatarWithCamera(modifier: Modifier = Modifier) {
  Box(modifier = modifier.size(68.dp)) {
    Box(
        modifier =
            Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = Icons.Rounded.Person,
          contentDescription = null,
          modifier = Modifier.size(44.dp),
          tint = MaterialTheme.colorScheme.onSecondary,
      )
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.size(26.dp).align(Alignment.BottomEnd),
    ) {
      Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Change photo",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun LogoutButton(
    component: ProfileComponent,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable { component.onLogoutClicked() }
              .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
        imageVector = Icons.AutoMirrored.Default.ExitToApp,
        contentDescription = "Log out",
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
        text = "Log out",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
  OpenSplitTheme {
    Surface {
      ProfileScreen(
          component = FakeProfileComponent(),
      )
    }
  }
}
