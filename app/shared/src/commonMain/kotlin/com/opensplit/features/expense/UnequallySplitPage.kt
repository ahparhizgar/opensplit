package com.opensplit.features.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme

@Composable
fun UnequallySplitPage(component: UnequallySplitComponent, onDone: () -> Unit = {}) {
  val uiState by component.uiState.subscribeAsState()
  val focusManager = LocalFocusManager.current
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier =
            Modifier.weight(1f).onKeyEvent {
              if (it.isCmdEnter()) {
                onDone()
                true
              } else {
                false
              }
            }
    ) {
      itemsIndexed(component.initialParticipants) { index, id ->
        ListItem(
            headlineContent = { Text(id) },
            trailingContent = {
              val style = MaterialTheme.typography.bodyMedium
              TextField(
                  modifier = Modifier.width(120.dp),
                  value = uiState.amounts[id] ?: "",
                  onValueChange = { component.onParticipantAmountChanged(id, it) },
                  placeholder = { Text(text = "0.00", style = style) },
                  prefix = { Text("IRR ") },
                  colors = transparentTextFieldColors,
                  keyboardOptions =
                      KeyboardOptions(
                          keyboardType = KeyboardType.Decimal,
                          imeAction =
                              if (index < component.initialParticipants.lastIndex) ImeAction.Next
                              else ImeAction.Done,
                      ),
                  keyboardActions =
                      KeyboardActions(
                          onNext = { focusManager.moveFocus(FocusDirection.Down) },
                          onDone = { onDone() },
                      ),
                  textStyle = style,
                  singleLine = true,
              )
            },
        )
      }
    }
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      val diff = uiState.remainingAmount
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Total: IRR ${component.totalAmount - uiState.remainingAmount}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text =
                if (kotlin.math.abs(diff) == 0.0) "All settled"
                else if (diff > 0) "IRR ${diff} left" else "IRR ${-diff} over",
            style = MaterialTheme.typography.labelSmall,
            color =
                if (kotlin.math.abs(diff) < 0.01) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@Preview
@Composable
private fun UnequallySplitPreview() {
  OpenSplitTheme {
    UnequallySplitPage(
        FakeUnequallySplitComponent(
            initialParticipants = listOf("Alice", "Bob"),
            totalAmount = 1000.0,
            uiState =
                UnequallySplitUiState(amounts = mapOf("Alice" to "600"), remainingAmount = 400.0),
        )
    )
  }
}
