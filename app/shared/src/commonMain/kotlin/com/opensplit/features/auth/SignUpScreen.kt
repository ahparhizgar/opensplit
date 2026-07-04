package com.opensplit.features.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(component: SignUpComponent) {
  val state by component.state.collectAsState()
  var passwordVisible by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = component::onBackClicked) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
        )
      }
  ) { padding ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = state.fullName,
            onValueChange = component::onFullNameChanged,
            label = { Text("Full name") },
            modifier = Modifier.weight(1f),
            isError = state.fieldErrors.containsKey("fullName"),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier =
                Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFF5F5F5)).clickable {
                  /* Profile pic button */
                },
            contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
          value = state.email,
          onValueChange = component::onEmailChanged,
          label = { Text("Email address") },
          modifier = Modifier.fillMaxWidth(),
          isError = state.fieldErrors.containsKey("email"),
      )

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
          value = state.password,
          onValueChange = component::onPasswordChanged,
          label = { Text("Password") },
          modifier = Modifier.fillMaxWidth(),
          visualTransformation =
              if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                  if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = null,
              )
            }
          },
          isError = state.fieldErrors.containsKey("password"),
          supportingText = { Text("Minimum 8 characters") },
      )

      Spacer(modifier = Modifier.height(16.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = "+98",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.width(100.dp),
            trailingIcon = { Icon(Icons.Default.ArrowBack, null) }, // Mock dropdown
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = state.phoneNumber,
            onValueChange = component::onPhoneChanged,
            label = { Text("Phone number") },
            modifier = Modifier.weight(1f),
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = "I use GBP (£) as my currency. Change »",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
      }

      Spacer(modifier = Modifier.height(48.dp))

      Text(
          text = "By signing up, you accept the OpenSplit Terms of Service and Privacy Policy.",
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
          onClick = component::onDoneClicked,
          modifier = Modifier.fillMaxWidth().height(56.dp),
          shape = RoundedCornerShape(8.dp),
          enabled = !state.isSubmitting,
      ) {
        Text("Done", style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}

@Composable
@Preview
fun SignUpScreenPreview() {
  MaterialTheme { Surface { SignUpScreen(FakeSignUpComponent()) } }
}
