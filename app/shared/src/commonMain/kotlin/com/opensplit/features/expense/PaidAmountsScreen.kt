package com.opensplit.features.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.ui.OpenSplitTheme
import kotlin.math.abs

@Composable
fun PaidAmountsScreen(component: PaidAmountsComponent, modifier: Modifier = Modifier) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(uiState.allParticipantAmounts) { participant ->
        ListItem(
            // Todo: convert to name
            headlineContent = { Text(participant.userId) },
            trailingContent = {
              OutlinedTextField(
                  modifier = Modifier.width(120.dp),
                  value = participant.value,
                  onValueChange = { component.onParticipantAmountChanged(participant.userId, it) },
                  prefix = { Text("IRR ") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              )
            },
        )
      }
    }

    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      val totalPaid = uiState.allParticipantAmounts.sumOf { it.value.toDoubleOrNull() ?: 0.0 }
      val amount = uiState.goalAmount
      if (amount != null) {
        val diff = amount - totalPaid

        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              text = "IRR $totalPaid of IRR $amount",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = if (abs(diff) < 0.01) "All settled" else "IRR $diff left",
              style = MaterialTheme.typography.labelSmall,
              color =
                  if (abs(diff) < 0.01) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun PaidAmountsPreview() {
  OpenSplitTheme { PaidAmountsScreen(FakePaidAmountsComponent()) }
}
