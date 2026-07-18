package com.opensplit.features.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(component: AddExpenseComponent) {
  val uiState by component.uiState.subscribeAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Add Expense") },
            navigationIcon = {
              IconButton(onClick = component::onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
        )
      }
  ) { padding ->
    Column(
        modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      OutlinedTextField(
          value = uiState.title,
          onValueChange = component::onTitleChanged,
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth(),
          isError = uiState.fieldErrors.containsKey("title"),
          supportingText = uiState.fieldErrors["title"]?.let { { Text(it) } },
      )

      OutlinedTextField(
          value = uiState.amount,
          onValueChange = component::onAmountChanged,
          label = { Text("Amount") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          isError = uiState.fieldErrors.containsKey("amount"),
          supportingText = uiState.fieldErrors["amount"]?.let { { Text(it) } },
      )

      Spacer(modifier = Modifier.weight(1f))

      Button(
          onClick = { component.onSaveClicked() },
          modifier = Modifier.fillMaxWidth(),
          enabled = !uiState.isLoading,
      ) {
        if (uiState.isLoading) {
          CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = MaterialTheme.colorScheme.onPrimary,
          )
        } else {
          Text("Save Expense")
        }
      }
    }
  }
}

@Preview
@Composable
fun AddExpenseScreenPreview() {
  OpenSplitTheme { AddExpenseScreen(component = FakeAddExpenseComponent()) }
}
