package com.opensplit.features.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(component: ResetPasswordComponent, modifier: Modifier = Modifier) {
  val state by component.state.collectAsState()

  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = { Text("Reset password") },
            navigationIcon = {
              IconButton(onClick = component::onBackClicked) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
              }
            },
        )
      },
  ) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
          "Reset your password",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
          "Enter your email address or phone number and we'll send you a link to reset your password.",
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(24.dp))

      TabRow(selectedTabIndex = if (state.selectedTab == ResetPasswordTab.Email) 0 else 1) {
        Tab(
            selected = state.selectedTab == ResetPasswordTab.Email,
            onClick = { component.onTabChanged(ResetPasswordTab.Email) },
        ) {
          Text("Email", modifier = Modifier.padding(12.dp))
        }
        Tab(
            selected = state.selectedTab == ResetPasswordTab.Phone,
            onClick = { component.onTabChanged(ResetPasswordTab.Phone) },
        ) {
          Text("Phone number", modifier = Modifier.padding(12.dp))
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      OutlinedTextField(
          value = state.email,
          onValueChange = component::onEmailChanged,
          label = { Text("Your email address") },
          modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(32.dp))

      Button(
          onClick = component::onResetClicked,
          modifier = Modifier.fillMaxWidth().height(56.dp),
          shape = RoundedCornerShape(8.dp),
          colors =
              ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF66CDAA)
              ), // Minty green as in screenshot
      ) {
        Text("Reset password", style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}

@Composable
@Preview
fun ResetPasswordScreenPreview() {
  MaterialTheme {
    Surface {
      ResetPasswordScreen(
          FakeResetPasswordComponent(),
      )
    }
  }
}
