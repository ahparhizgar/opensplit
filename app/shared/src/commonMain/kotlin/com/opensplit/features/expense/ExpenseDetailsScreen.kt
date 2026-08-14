package com.opensplit.features.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.opensplit.domain.FakeExpenseFactory
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.domain.ParticipantShare
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailsScreen(
    component: ExpenseDetailsComponent,
    modifier: Modifier = Modifier,
) {
  val uiState by component.uiState.subscribeAsState()
  val expense = uiState.expense

  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = { component.onBackClicked() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            actions = {
              IconButton(onClick = { component.onAddReceiptClicked() }) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Receipt")
              }
              IconButton(onClick = { component.onDeleteClicked() }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
              }
              IconButton(onClick = { component.onEditClicked() }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
              }
            },
        )
      },
      bottomBar = {
        Surface(tonalElevation = 2.dp) {
          Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            TextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                placeholder = { Text("Add a comment") },
                shape = RoundedCornerShape(24.dp),
                colors =
                    TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { /* TODO */ }) {
              Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
          }
        }
      },
  ) { padding ->
    if (expense != null) {
      Column(
          modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Surface(
              modifier = Modifier.size(40.dp),
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                  Icons.Default.Description,
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
              )
            }
          }
          Spacer(Modifier.height(16.dp))
          Text(
              text = expense.title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = "IRR ${expense.amount.formatAmount()}",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = "Added by you on Aug 6, 2026",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Spacer(Modifier.height(24.dp))

          Row(verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF00A388)))
            Spacer(Modifier.width(16.dp))
            Column {
              val payerMember = uiState.householdMembers.find { it.userId == expense.payerId }
              val payerName =
                  if (payerMember?.isCurrentUser == true) "You"
                  else payerMember?.name ?: expense.payerId
              Text(
                  text = "$payerName paid IRR ${expense.amount.formatAmount()}",
                  style = MaterialTheme.typography.bodyLarge,
              )
              Spacer(Modifier.height(4.dp))
              expense.participants.forEach { participant ->
                val member = uiState.householdMembers.find { it.userId == participant.userId }
                val isMe = member?.isCurrentUser == true
                val name = if (isMe) "You" else member?.name ?: participant.userId
                val verb = if (isMe) "owe" else "owes"
                if (participant.consumedShare > 0) {
                  Text(
                      text = "$name $verb IRR ${participant.consumedShare.formatAmount()}",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }
    } else if (uiState.isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }
  }
}

@Composable
fun TrendItem(month: String, amount: Double, progress: Float) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = month, modifier = Modifier.width(40.dp), style = MaterialTheme.typography.bodySmall)
    Box(modifier = Modifier.height(12.dp).weight(1f).background(Color.Transparent)) {
      Box(
          modifier =
              Modifier.fillMaxHeight()
                  .fillMaxWidth(progress)
                  .background(Color.LightGray.copy(alpha = 0.5f))
      )
    }
    Spacer(Modifier.width(8.dp))
    Text(
        text = "IRR ${amount.formatAmount()}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
    )
  }
}

@Preview
@Composable
fun ExpenseDetailsScreenPreview() {
  val fakeExpense =
      FakeExpenseFactory.create(
          title = "Test",
          amount = 0.67,
          participants =
              listOf(
                  ParticipantShare("user-1", 0.67, 0.34),
                  ParticipantShare("user-2", 0.0, 0.33),
              ),
      )
  val fakeMembers =
      listOf(
          FakeMemberFactory.create(userId = "user-1", name = "You", isCurrentUser = true),
          FakeMemberFactory.create(userId = "user-2", name = "Ali B."),
      )
  OpenSplitTheme(darkTheme = true) {
    ExpenseDetailsScreen(
        component =
            FakeExpenseDetailsComponent(
                uiState =
                    ExpenseDetailsUiState(expense = fakeExpense, householdMembers = fakeMembers)
            )
    )
  }
}
