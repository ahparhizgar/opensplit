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
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.ui.OpenSplitTheme

@Composable
fun SharesSplitPage(component: SharesSplitComponent, onDone: () -> Unit = {}) {
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
      itemsIndexed(component.participants) { index, member ->
        ListItem(
            headlineContent = { Text(if (member.isCurrentUser) "you" else member.name) },
            trailingContent = {
              val style = MaterialTheme.typography.bodyMedium
              TextField(
                  modifier = Modifier.width(100.dp),
                  value = uiState.shares[member.userId] ?: "",
                  onValueChange = { component.onParticipantSharesChanged(member.userId, it) },
                  placeholder = { Text(text = "0", style = style) },
                  colors = transparentTextFieldColors,
                  keyboardOptions =
                      KeyboardOptions(
                          keyboardType = KeyboardType.Number,
                          imeAction =
                              if (index < component.participants.lastIndex) ImeAction.Next
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
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Total shares: ${uiState.totalShares}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Preview
@Composable
private fun SharesSplitPreview() {
  OpenSplitTheme {
    SharesSplitPage(
        FakeSharesSplitComponent(
            participants = FakeMemberFactory.createList(),
            uiState = SharesSplitUiState(shares = mapOf("Alice" to "2", "Bob" to "1")),
        )
    )
  }
}
