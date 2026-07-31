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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.ui.OpenSplitTheme

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
                  when (val instance = component.stack.value.active.instance) {
                    is AddExpenseComponent.Child.Main -> "Add Expense"
                    is AddExpenseComponent.Child.WhoPaid -> "Who paid?"
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
                    } else if (activeChild is AddExpenseComponent.Child.PaidAmounts) {
                      activeChild.component.onDone()
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
        is AddExpenseComponent.Child.WhoPaid -> WhoPaidScreen(instance.component)
        is AddExpenseComponent.Child.PaidAmounts -> PaidAmountsScreen(instance.component)
        is AddExpenseComponent.Child.QuickSplitSelection -> QuickSplitScreen(instance.component)
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

val previewParticipants = listOf("user1-----", "user2++++++++", "user3#####")

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
