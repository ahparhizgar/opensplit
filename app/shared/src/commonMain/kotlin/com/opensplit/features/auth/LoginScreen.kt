package com.opensplit.features.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(component: LoginComponent, modifier: Modifier = Modifier) {
  val state by component.state.collectAsState()
  var passwordVisible by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current

  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = component::onBackClicked) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
              }
            },
        )
      },
  ) { padding ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
    ) {
      Text("Log in", style = MaterialTheme.typography.headlineMedium)
      Spacer(modifier = Modifier.height(32.dp))

      OutlinedTextField(
          value = state.email,
          onValueChange = component::onEmailChanged,
          label = { Text("Email address") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          isError = state.fieldErrors.containsKey("email"),
          keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
          keyboardActions =
              KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
      )
      state.fieldErrors["email"]?.let {
        Text(
            it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
          value = state.password,
          onValueChange = component::onPasswordChanged,
          label = { Text("Password") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
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
          keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { component.onLoginClicked() }),
      )
      state.fieldErrors["password"]?.let {
        Text(
            it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
      }

      state.generalError?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      Button(
          onClick = component::onLoginClicked,
          modifier = Modifier.fillMaxWidth().height(56.dp),
          shape = RoundedCornerShape(8.dp),
          enabled = !state.isSubmitting,
      ) {
        Text("Log in", style = MaterialTheme.typography.titleMedium)
      }

      Spacer(modifier = Modifier.height(24.dp))

      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            "Forgot your password?",
            color = Color(0xFF198754),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { component.onForgotPasswordClicked() },
        )
      }
    }
  }
}

@Composable
@Preview
fun LoginScreenPreview() {
  MaterialTheme {
    Surface {
      LoginScreen(
          FakeLoginComponent(),
      )
    }
  }
}
