package com.opensplit.features.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.ui.OpenSplitTheme
import kotlin.math.abs

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
                  when (component.stack.value.active.instance) {
                    is AddExpenseComponent.Child.Main -> "Add Expense"
                    is AddExpenseComponent.Child.PayerSelection -> "Who paid?"
                    is AddExpenseComponent.Child.PaidAmounts -> "Enter paid amounts"
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
              IconButton(
                  onClick = {
                    val activeChild = component.stack.value.active.instance
                    if (activeChild is AddExpenseComponent.Child.Main) {
                      component.onSaveClicked()
                    } else if (activeChild is AddExpenseComponent.Child.AdjustSplit) {
                      activeChild.component.onDoneClicked()
                    } else {
                      component.onDoneClicked()
                    }
                  }
              ) {
                Icon(Icons.Default.Check, contentDescription = "Done")
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
        is AddExpenseComponent.Child.PaidAmounts -> PaidAmountsScreen(instance.component)
        is AddExpenseComponent.Child.QuickSplitSelection ->
            QuickSplitSelectionScreen(instance.component)
        is AddExpenseComponent.Child.AdjustSplit -> AdjustSplitScreen(instance.component)
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
        label = { Text("Description") },
        placeholder = { Text("Enter a description") },
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.fieldErrors.containsKey("title"),
        supportingText = uiState.fieldErrors["title"]?.let { { Text(it) } },
    )

    if (uiState.payAmounts is PayAmountsUiState.OnePerson) {
      OutlinedTextField(
          value = uiState.payAmounts.amount,
          onValueChange = component::onAmountChanged,
          placeholder = { Text("0.00") },
          prefix = { Text("IRR ") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          isError = uiState.fieldErrors.containsKey("amount"),
          supportingText = uiState.fieldErrors["amount"]?.let { { Text(it) } },
      )
    }

    val payerText =
        when (uiState.payAmountsDomain) {
          is PayAmounts.MultiplePeople -> "Multiple people"
          is PayAmounts.OnePerson -> {
            // TODO convert to name
            uiState.payAmountsDomain.userId
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
      Text(
          "Paid by $payerText and split ${if(uiState.splitMethod is SplitMethod.Equally) "equally" else "unequally"}."
      )
      Spacer(modifier = Modifier.size(8.dp))
      Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
    }

    Spacer(modifier = Modifier.weight(1f))

    if (uiState.isLoading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    }
  }
}

@Composable
private fun PayerSelectionScreen(component: AddExpenseComponent, uiState: AddExpenseUiState) {
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(uiState.allParticipants) { participant ->
      val payAmounts = uiState.payAmounts as PayAmountsUiState.OnePerson
      val isPayer = payAmounts.userId == participant
      ListItem(
          // Todo: convert to name
          headlineContent = { Text(participant) },
          trailingContent = { if (isPayer) Icon(Icons.Default.Check, contentDescription = null) },
          modifier =
              Modifier.clickable {
                component.setPaidAmounts(
                    PayAmountsUiState.OnePerson(participant, payAmounts.amount)
                )
                component.onDoneClicked()
              },
      )
    }
    item {
      ListItem(
          headlineContent = { Text("Multiple people") },
          modifier = Modifier.clickable { component.navigateToPaidAmounts() },
      )
    }
  }
}

@Composable
private fun PaidAmountsScreen(component: PaidAmountsComponent) {
  val uiState by component.uiState.subscribeAsState()
  Column(modifier = Modifier.fillMaxSize()) {
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

@Composable
private fun QuickSplitSelectionScreen(component: AddExpenseComponent) {
  val uiState by component.uiState.subscribeAsState()
  // TODO order is not guaranteed
  val you = uiState.allParticipants[0]
  val other = uiState.allParticipants[1]
  Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ListItem(
        modifier =
            Modifier.clickable {
              component.setPaidAmounts(
                  PayAmountsUiState.OnePerson(you, uiState.amountSum.toString())
              )
              component.setSplitMethod(SplitMethod.Equally(uiState.allParticipants))
              component.onBackClicked()
            },
        headlineContent = { Text("You paid, split equally.") },
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.setPaidAmounts(
                  PayAmountsUiState.OnePerson(you, uiState.amountSum.toString())
              )
              component.setSplitMethod(SplitMethod.Unequally(mapOf(other to uiState.amountSum)))
              component.onBackClicked()
            },
        headlineContent = { Text("You are owed the full amount.") },
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.setPaidAmounts(
                  PayAmountsUiState.OnePerson(other, uiState.amountSum.toString())
              )
              component.setSplitMethod(SplitMethod.Equally(uiState.allParticipants))
              component.onBackClicked()
            },
        headlineContent = { Text("$other paid, split equally.") },
    )
    ListItem(
        modifier =
            Modifier.clickable {
              component.setPaidAmounts(
                  PayAmountsUiState.OnePerson(other, uiState.amountSum.toString())
              )
              component.setSplitMethod(SplitMethod.Unequally(mapOf(you to uiState.amountSum)))
              component.onBackClicked()
            },
        headlineContent = { Text("$other is owed the full amount.") },
    )

    Button(
        onClick = { component.navigateToAdjustSplit() },
        modifier = Modifier.align(Alignment.CenterHorizontally),
    ) {
      Text("More options")
    }
  }
}

val previewParticipants = listOf("user1", "user2", "user3")

val previewUiState =
    AddExpenseUiState(
        title = "Dinner at restaurant",
        splitMethod = SplitMethod.Equally(previewParticipants.map { it }),
        allParticipants = previewParticipants,
        payAmounts = PayAmountsUiState.OnePerson(previewParticipants[0], "20"),
    )

@Preview
@Composable
private fun MainExpenseFormPreview() {
  OpenSplitTheme { AddExpenseScreen(FakeAddExpenseComponent(previewUiState)) }
}

@Preview
@Composable
private fun PayerSelectionPreview() {
  OpenSplitTheme {
    AddExpenseScreen(
        FakeAddExpenseComponent(
            uiState = previewUiState,
            childFactory = { AddExpenseComponent.Child.PayerSelection(it) },
        )
    )
  }
}

@Preview
@Composable
private fun PaidAmountsPreview() {
  OpenSplitTheme {
    AddExpenseScreen(
        FakeAddExpenseComponent(
            uiState = previewUiState,
            childFactory = { AddExpenseComponent.Child.PaidAmounts(FakePaidAmountsComponent()) },
        )
    )
  }
}

@Preview
@Composable
private fun QuickSplitPreview() {
  OpenSplitTheme {
    AddExpenseScreen(
        FakeAddExpenseComponent(
            uiState = previewUiState,
            childFactory = { AddExpenseComponent.Child.QuickSplitSelection(it) },
        )
    )
  }
}
