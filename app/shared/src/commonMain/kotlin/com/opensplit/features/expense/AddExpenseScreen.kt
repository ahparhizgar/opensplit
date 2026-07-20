package com.opensplit.features.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.dto.expense.SplitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(component: AddExpenseComponent, modifier: Modifier = Modifier) {
  val uiState by component.uiState.subscribeAsState()

  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = {
              Text(
                  when (val child = component.stack.value.active.instance) {
                    is AddExpenseComponent.Child.Main -> "Add Expense"
                    is AddExpenseComponent.Child.PayerSelection -> "Who paid?"
                    is AddExpenseComponent.Child.QuickSplitSelection ->
                        "How was this expense split?"
                    is AddExpenseComponent.Child.AdjustSplit -> "Adjust split"
                  }
              )
            },
            navigationIcon = {
              IconButton(onClick = component::onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            actions = {
              if (component.stack.value.active.instance !is AddExpenseComponent.Child.Main) {
                IconButton(onClick = component::onDoneClicked) {
                  Icon(Icons.Default.Check, contentDescription = "Done")
                }
              }
            },
        )
      },
  ) { padding ->
    Children(stack = component.stack, modifier = Modifier.padding(padding).fillMaxSize()) { child ->
      when (val instance = child.instance) {
        is AddExpenseComponent.Child.Main -> MainExpenseForm(instance.component, uiState)
        is AddExpenseComponent.Child.PayerSelection ->
            PayerSelectionScreen(instance.component, uiState)
        is AddExpenseComponent.Child.QuickSplitSelection ->
            QuickSplitSelectionScreen(instance.component, uiState)
        is AddExpenseComponent.Child.AdjustSplit -> AdjustSplitScreen(instance.component, uiState)
      }
    }
  }
}

@Composable
private fun MainExpenseForm(component: AddExpenseComponent, uiState: AddExpenseUiState) {
  Column(
      modifier = Modifier.padding(16.dp).fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    OutlinedTextField(
        value = uiState.title,
        onValueChange = component::onTitleChanged,
        label = { Text("Enter a description") },
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.fieldErrors.containsKey("title"),
        supportingText = uiState.fieldErrors["title"]?.let { { Text(it) } },
    )

    OutlinedTextField(
        value = uiState.amount,
        onValueChange = component::onAmountChanged,
        label = { Text("0.00") },
        prefix = { Text("IRR ") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = uiState.fieldErrors.containsKey("amount"),
        supportingText = uiState.fieldErrors["amount"]?.let { { Text(it) } },
    )

    val payersCount = uiState.participants.count { (it.paidAmount.toDoubleOrNull() ?: 0.0) > 0.0 }
    val payerText =
        when {
          payersCount > 1 -> "Multiple people"
          else -> {
            val payer = uiState.participants.find { (it.paidAmount.toDoubleOrNull() ?: 0.0) > 0.0 }
            payer?.name ?: "You"
          }
        }

    Button(
        onClick = { component.navigateToQuickSplit() },
        modifier = Modifier.fillMaxWidth(),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
      Text("Paid by $payerText and split ${uiState.splitType.name.lowercase()}.")
      Spacer(modifier = Modifier.size(8.dp))
      Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
    }

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

@Composable
private fun PayerSelectionScreen(component: AddExpenseComponent, uiState: AddExpenseUiState) {
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(uiState.participants) { participant ->
      val isPayer = (participant.paidAmount.toDoubleOrNull() ?: 0.0) > 0.0
      ListItem(
          headlineContent = { Text(participant.name) },
          trailingContent = { if (isPayer) Icon(Icons.Default.Check, contentDescription = null) },
          modifier =
              Modifier.clickable {
                uiState.participants.forEach {
                  component.onParticipantPaidAmountChanged(it.userId, "0")
                }
                component.onParticipantPaidAmountChanged(participant.userId, uiState.amount)
                component.onDoneClicked()
              },
      )
    }
    item {
      ListItem(
          headlineContent = { Text("Multiple people") },
          modifier =
              Modifier.clickable {
                // Navigate to multiple payers detailed screen if needed
              },
      )
    }
  }
}

@Composable
private fun QuickSplitSelectionScreen(component: AddExpenseComponent, uiState: AddExpenseUiState) {
  Column(modifier = Modifier.fillMaxSize()) {
    uiState.participants.forEach { participant ->
      ListItem(
          headlineContent = { Text("${participant.name} paid, split equally.") },
          modifier =
              Modifier.clickable {
                uiState.participants.forEach {
                  component.onParticipantPaidAmountChanged(it.userId, "0")
                }
                component.onParticipantPaidAmountChanged(participant.userId, uiState.amount)
                component.onSplitTypeChanged(SplitType.EQUALLY)
                component.onDoneClicked()
              },
      )
    }

    Spacer(modifier = Modifier.size(16.dp))

    Button(
        onClick = { component.navigateToAdjustSplit() },
        modifier = Modifier.align(Alignment.CenterHorizontally),
    ) {
      Text("More options")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdjustSplitScreen(component: AddExpenseComponent, uiState: AddExpenseUiState) {
  Column(modifier = Modifier.fillMaxSize()) {
    SecondaryTabRow(selectedTabIndex = uiState.splitType.ordinal) {
      SplitType.entries.forEach { type ->
        Tab(
            selected = uiState.splitType == type,
            onClick = { component.onSplitTypeChanged(type) },
            text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
        )
      }
    }

    LazyColumn(modifier = Modifier.weight(1f)) {
      items(uiState.participants) { participant ->
        ListItem(
            headlineContent = { Text(participant.name) },
            supportingContent = { Text("IRR ${participant.owedAmount}") },
            trailingContent = {
              when (uiState.splitType) {
                SplitType.EQUALLY -> {
                  Checkbox(
                      checked = participant.isIncluded,
                      onCheckedChange = {
                        component.onParticipantInclusionChanged(participant.userId, it)
                      },
                  )
                }
                SplitType.EXACT -> {
                  OutlinedTextField(
                      value = participant.owedAmount,
                      onValueChange = {
                        component.onParticipantOwedAmountChanged(participant.userId, it)
                      },
                      modifier = Modifier.width(100.dp),
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  )
                }
                SplitType.PERCENTAGE -> {
                  OutlinedTextField(
                      value = participant.percentage,
                      onValueChange = {
                        component.onParticipantPercentageChanged(participant.userId, it)
                      },
                      modifier = Modifier.width(100.dp),
                      suffix = { Text("%") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  )
                }
                SplitType.SHARES -> {
                  OutlinedTextField(
                      value = participant.shares,
                      onValueChange = {
                        component.onParticipantSharesChanged(participant.userId, it)
                      },
                      modifier = Modifier.width(100.dp),
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  )
                }
                SplitType.ADJUSTMENT -> {
                  OutlinedTextField(
                      value = participant.adjustment,
                      onValueChange = {
                        component.onParticipantAdjustmentChanged(participant.userId, it)
                      },
                      modifier = Modifier.width(100.dp),
                      prefix = { Text("+ ") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  )
                }
              }
            },
        )
      }
    }

    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
      val totalOwed = uiState.participants.sumOf { it.owedAmount.toDoubleOrNull() ?: 0.0 }
      val amount = uiState.amount.toDoubleOrNull() ?: 0.0
      val diff = amount - totalOwed

      Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "IRR ${totalOwed} of IRR ${amount}",
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
