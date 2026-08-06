package com.opensplit.features.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.ui.colorSchemeExtended
import com.opensplit.util.formatAmount

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
                    is AddExpenseComponent.Child.WhoPaid -> "Who paid?"
                    is AddExpenseComponent.Child.PaidAmounts -> "Enter paid amounts"
                    is AddExpenseComponent.Child.QuickSplitSelection ->
                        "How was this expense split?"
                    is AddExpenseComponent.Child.MoreSplitOptions -> "Adjust split"
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
                    } else if (activeChild is AddExpenseComponent.Child.MoreSplitOptions) {
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
        is AddExpenseComponent.Child.MoreSplitOptions -> MoreSplitOptionsScreen(instance.component)
      }
    }
  }
}

@Composable
private fun MainExpenseForm(component: AddExpenseComponent, uiState: AddExpenseUiState) {
  Column(
      modifier = Modifier.padding(16.dp).fillMaxSize(),
  ) {
    ParticipantHeader(uiState.participants)

    Spacer(modifier = Modifier.height(32.dp))
    val textFieldColors =
        TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
        )

    // Amount
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      AmountIcon()
      Spacer(modifier = Modifier.width(16.dp))
      if (uiState.payAmounts is PayAmountsUiState.OnePerson) {
        OutlinedTextField(
            value = uiState.payAmounts.amount,
            onValueChange = component::onAmountChanged,
            placeholder = { Text("0.00", fontSize = 24.sp) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = uiState.fieldErrors.containsKey("amount"),
            supportingText = uiState.fieldErrors["amount"]?.let { { Text(it) } },
            textStyle = MaterialTheme.typography.headlineSmall,
            colors = textFieldColors,
        )
      } else {
        // When multiple people paid, we show the sum, but it's edited in PaidAmountsScreen
        Text(
            text = uiState.amountSum.formatAmount(),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Description
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      DescriptionIcon()
      Spacer(modifier = Modifier.width(16.dp))
      OutlinedTextField(
          value = uiState.title,
          onValueChange = component::onTitleChanged,
          placeholder = { Text("Enter a description", fontSize = 18.sp) },
          modifier = Modifier.fillMaxWidth(),
          isError = uiState.fieldErrors.containsKey("title"),
          supportingText = uiState.fieldErrors["title"]?.let { { Text(it) } },
          textStyle = MaterialTheme.typography.headlineSmall,
          colors = textFieldColors,
      )
    }

    Spacer(modifier = Modifier.height(32.dp))

    if (
        uiState.summaryText != null || (uiState.participants.size == 2 && uiState.amountSum == 0.0)
    ) {
      QuickSplitButton(uiState, component)
    } else {
      SplitSentence(uiState, component)
    }

    Spacer(modifier = Modifier.weight(1f))

    if (uiState.isLoading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    }

    BottomActionRow(uiState.householdName)
  }
}

@Composable
private fun AmountIcon() {
  Box(
      modifier =
          Modifier.size(62.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center,
  ) {
    Text(text = "IRR", style = MaterialTheme.typography.titleLarge)
  }
}

@Composable
private fun DescriptionIcon() {
  Box(
      modifier =
          Modifier.size(62.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center,
  ) {
    Icon(
        Icons.AutoMirrored.Filled.Note,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
    )
  }
}

@Composable
private fun ParticipantHeader(participants: List<com.opensplit.domain.Member>) {
  val others = participants.filter { !it.isCurrentUser }

  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
        buildAnnotatedString {
          append("With ")
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("you") }
          append(" and: ")
        }
    )

    if (others.size == 1) {
      Text(others[0].name, fontWeight = FontWeight.Bold)
    } else if (others.size > 1) {
      ClickableLabel(
          onClick = { /* Could show participant list */ },
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "+${others.size}")
        }
      }
    }
  }
}

@Composable
private fun QuickSplitButton(uiState: AddExpenseUiState, component: AddExpenseComponent) {
  val text =
      if (uiState.summaryText != null) {
        uiState.summaryText!!
      } else {
        val payerText =
            when (uiState.payAmountsDomain) {
              is PayAmounts.MultiplePeople -> "2+ people"
              is PayAmounts.OnePerson -> uiState.getParticipantName(uiState.payAmountsDomain.userId)
            }
        val splitLabel = if (uiState.splitMethod is SplitMethod.Equally) "equally" else "unequally"
        "Paid by $payerText and split $splitLabel"
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
    Text(text)
    Spacer(modifier = Modifier.size(8.dp))
    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
  }
}

@Composable
private fun SplitSentence(uiState: AddExpenseUiState, component: AddExpenseComponent) {
  val payerLabel =
      when (uiState.payAmountsDomain) {
        is PayAmounts.MultiplePeople -> "2+ people"
        is PayAmounts.OnePerson -> uiState.getParticipantName(uiState.payAmountsDomain.userId)
      }

  val splitLabel = if (uiState.splitMethod is SplitMethod.Equally) "equally" else "unequally"

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text("Paid by ")
    ClickableLabel(
        onClick = {
          if (uiState.payAmountsDomain is PayAmounts.MultiplePeople) {
            component.navigateToPaidAmounts()
          } else {
            component.navigateToPayerSelection()
          }
        }
    ) {
      Text(payerLabel)
    }
    Text(" and split ")
    ClickableLabel(onClick = { component.navigateToAdjustSplit() }) { Text(splitLabel) }
  }
}

@Composable
private fun ClickableLabel(onClick: () -> Unit, content: @Composable () -> Unit) {
  Box(
      modifier =
          Modifier.padding(horizontal = 4.dp)
              .clip(RoundedCornerShape(4.dp))
              .border(
                  width = 1.dp,
                  color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                  shape = RoundedCornerShape(4.dp),
              )
              .clickable(onClick = onClick)
              .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    content()
  }
}

@Composable
private fun BottomActionRow(householdName: String) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
          Icons.Default.Group,
          contentDescription = null,
          tint = MaterialTheme.colorSchemeExtended.youOwe,
          modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(householdName, style = MaterialTheme.typography.bodyMedium)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF4EB8C7))
      Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF9C27B0))
      Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null, tint = Color(0xFF4CAF50))
    }
  }
}

val previewParticipants = FakeMemberFactory.createList()

val previewUiState =
    AddExpenseUiState(
        householdName = " Home",
        title = "Dinner at restaurant",
        splitMethod = SplitMethod.Equally(previewParticipants.map { it.userId }),
        allParticipants = previewParticipants.map { it.userId },
        participants = previewParticipants,
        payAmounts = PayAmountsUiState.OnePerson(previewParticipants[0].userId, "20"),
    )

@Preview
@Composable
private fun MainExpenseFormPreview() {
  OpenSplitTheme { AddExpenseScreen(FakeAddExpenseComponent(previewUiState)) }
}

@Preview
@Composable
private fun MainExpenseForm2Preview() {
  OpenSplitTheme {
    AddExpenseScreen(
        FakeAddExpenseComponent(
            previewUiState.copy(participants = FakeMemberFactory.createListWith3Members())
        )
    )
  }
}
