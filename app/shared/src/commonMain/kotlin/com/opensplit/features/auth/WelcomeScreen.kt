package com.opensplit.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(component: WelcomeComponent) {
  Column(
      modifier = Modifier.fillMaxSize().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(modifier = Modifier.weight(1f))

    // Logo Placeholder
    Box(
        modifier =
            Modifier.size(150.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier.size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "OpenSplit",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
        )
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    Button(
        onClick = component::onSignUpClicked,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
      Text("Sign up", style = MaterialTheme.typography.titleMedium)
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = component::onLoginClicked,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
      Text("Log in", style = MaterialTheme.typography.titleMedium)
    }

    Spacer(modifier = Modifier.height(48.dp))

    Row(modifier = Modifier.padding(bottom = 16.dp), horizontalArrangement = Arrangement.Center) {
      Text(
          "Terms",
          style = MaterialTheme.typography.bodySmall,
          textDecoration = TextDecoration.Underline,
      )
      Text(" | ", style = MaterialTheme.typography.bodySmall)
      Text(
          "Privacy Policy",
          style = MaterialTheme.typography.bodySmall,
          textDecoration = TextDecoration.Underline,
      )
      Text(" | ", style = MaterialTheme.typography.bodySmall)
      Text(
          "Contact us",
          style = MaterialTheme.typography.bodySmall,
          textDecoration = TextDecoration.Underline,
      )
    }
  }
}

@Composable
@Preview
fun WelcomeScreenPreview() {
  MaterialTheme { Surface { WelcomeScreen(FakeWelcomeComponent()) } }
}
