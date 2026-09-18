package com.opensplit.features.group.createjoin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.ui.OpenSplitTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateGroupScreen(
    component: CreateGroupComponent,
    modifier: Modifier = Modifier,
) {
  val state by component.uiState.collectAsState()

  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = { Text("Create Group") },
            navigationIcon = {
              IconButton(onClick = { component.onBackClicked() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
        )
      },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
      OutlinedTextField(
          value = state.groupName,
          onValueChange = component::updateGroupName,
          label = { Text("Group Name") },
          isError = state.fieldErrors.containsKey("name"),
          modifier = Modifier.fillMaxWidth().testTag("group-name"),
          singleLine = true,
      )
      state.fieldErrors["name"]?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("group-name-error"),
        )
      }

      state.generalError?.let {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("group-general-error"),
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
          onClick = { component.submit() },
          modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("group-submit"),
          shape = ButtonDefaults.squareShape,
          enabled = !state.isSubmitting,
      ) {
        Text("Create")
      }
    }
  }
}

@Preview
@Composable
private fun CreateGroupScreenPreview() {
  OpenSplitTheme { CreateGroupScreen(FakeCreateGroupComponent()) }
}
