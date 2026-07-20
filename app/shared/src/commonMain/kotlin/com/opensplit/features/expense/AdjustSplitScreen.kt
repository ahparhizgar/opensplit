package com.opensplit.features.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.dto.expense.SplitType
import com.opensplit.ui.OpenSplitTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustSplitScreen(component: AdjustSplitComponent) {
  val pagerState = rememberPagerState(pageCount = { SplitType.entries.size })
  val scope = rememberCoroutineScope()

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }
        .collect { component.onTabChanged(SplitType.entries[it]) }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    SecondaryScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
      SplitType.entries.forEachIndexed { index, type ->
        Tab(
            selected = pagerState.currentPage == index,
            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
            text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
        )
      }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.weight(1f),
        key = { SplitType.entries[it].name },
    ) { page ->
      when (SplitType.entries[page]) {
        SplitType.EQUALLY -> EquallySplitPage(component.equallyComponent)
        SplitType.EXACT -> ExactSplitPage(component.exactComponent)
        SplitType.PERCENTAGE -> PercentageSplitPage(component.percentageComponent)
        SplitType.SHARES -> SharesSplitPage(component.sharesComponent)
        SplitType.ADJUSTMENT -> AdjustmentSplitPage(component.adjustmentComponent)
      }
    }
  }
}

@Composable
private fun EquallySplitPage(component: EquallySplitComponent) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(uiState.participants) { participant ->
        ListItem(
            headlineContent = { Text(participant.name) },
            supportingContent = { Text("IRR ${participant.owedAmount}") },
            trailingContent = {
              Checkbox(
                  checked = participant.isIncluded,
                  onCheckedChange = {
                    component.onParticipantInclusionChanged(participant.userId, it)
                  },
              )
            },
        )
      }
    }
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      val includedCount = uiState.participants.count { it.isIncluded }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Split equally among $includedCount people",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun ExactSplitPage(component: ExactSplitComponent) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(uiState.participants) { participant ->
        ListItem(
            headlineContent = { Text(participant.name) },
            trailingContent = {
              TextField(
                  modifier = Modifier.width(120.dp),
                  value =
                      if ((participant.owedAmount.toDoubleOrNull() ?: 0.0) == 0.0) ""
                      else participant.owedAmount,
                  onValueChange = { component.onParticipantAmountChanged(participant.userId, it) },
                  placeholder = { Text("0.00") },
                  prefix = { Text("IRR ") },
                  colors = transparentTextFieldColors,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              )
            },
        )
      }
    }
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      val diff = uiState.remainingAmount
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Total: IRR ${uiState.totalAmount}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (kotlin.math.abs(diff) < 0.01) "All settled" else "IRR ${diff} left",
            style = MaterialTheme.typography.labelSmall,
            color =
                if (kotlin.math.abs(diff) < 0.01) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@Composable
private fun PercentageSplitPage(component: PercentageSplitComponent) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(uiState.participants) { participant ->
        ListItem(
            headlineContent = { Text(participant.name) },
            supportingContent = { Text("IRR ${participant.owedAmount}") },
            trailingContent = {
              TextField(
                  modifier = Modifier.width(100.dp),
                  value =
                      if ((participant.percentage.toDoubleOrNull() ?: 0.0) == 0.0) ""
                      else participant.percentage,
                  onValueChange = {
                    component.onParticipantPercentageChanged(participant.userId, it)
                  },
                  placeholder = { Text("0") },
                  suffix = { Text("%") },
                  colors = transparentTextFieldColors,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              )
            },
        )
      }
    }
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      val total = uiState.totalPercentage
      val diff = 100.0 - total
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Total: $total%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (kotlin.math.abs(diff) < 0.01) "100% total" else "$diff% left",
            style = MaterialTheme.typography.labelSmall,
            color =
                if (kotlin.math.abs(diff) < 0.01) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@Composable
private fun SharesSplitPage(component: SharesSplitComponent) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(uiState.participants) { participant ->
        ListItem(
            headlineContent = { Text(participant.name) },
            supportingContent = { Text("IRR ${participant.owedAmount}") },
            trailingContent = {
              TextField(
                  modifier = Modifier.width(100.dp),
                  value =
                      if ((participant.shares.toDoubleOrNull() ?: 0.0) == 0.0) ""
                      else participant.shares,
                  onValueChange = { component.onParticipantSharesChanged(participant.userId, it) },
                  placeholder = { Text("0") },
                  colors = transparentTextFieldColors,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

@Composable
private fun AdjustmentSplitPage(component: AdjustmentSplitComponent) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(uiState.participants) { participant ->
        ListItem(
            headlineContent = { Text(participant.name) },
            supportingContent = { Text("IRR ${participant.owedAmount}") },
            trailingContent = {
              TextField(
                  modifier = Modifier.width(120.dp),
                  value =
                      if ((participant.adjustment.toDoubleOrNull() ?: 0.0) == 0.0) ""
                      else participant.adjustment,
                  onValueChange = {
                    component.onParticipantAdjustmentChanged(participant.userId, it)
                  },
                  placeholder = { Text("0.00") },
                  prefix = { Text("+ IRR ") },
                  colors = transparentTextFieldColors,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              )
            },
        )
      }
    }
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Total adjustments: IRR ${uiState.totalAdjustment}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

val transparentTextFieldColors
  @Composable
  get() =
      TextFieldDefaults.colors(
          focusedContainerColor = Color.Transparent,
          unfocusedContainerColor = Color.Transparent,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent,
      )

@Preview
@Composable
private fun AdjustSplitPreview() {
  OpenSplitTheme { AdjustSplitScreen(FakeAdjustSplitComponent()) }
}
