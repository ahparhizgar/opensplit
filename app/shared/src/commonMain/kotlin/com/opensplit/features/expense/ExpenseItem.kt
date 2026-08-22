package com.opensplit.features.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.domain.Expense
import com.opensplit.domain.Member
import com.opensplit.domain.ParticipantShare
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.ui.colorSchemeExtended
import com.opensplit.util.formatAmount
import kotlin.time.Instant

@Composable
fun ExpenseItem(
    expense: Expense,
    members: List<Member>,
    currentUserId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val myShare = expense.participants.find { it.userId == currentUserId }
  val netBalance = myShare?.netBalance ?: 0.0

  val statusText =
      when {
        netBalance > 0 -> "you lent"
        netBalance < 0 -> "you borrowed"
        else -> "settled"
      }

  val statusColor =
      when {
        netBalance > 0 -> MaterialTheme.colorSchemeExtended.youAreOwed
        netBalance < 0 -> MaterialTheme.colorSchemeExtended.youOwe
        else -> MaterialTheme.colorScheme.onSurfaceVariant
      }

  val displayAmount = if (netBalance < 0) -netBalance else netBalance

  // Simple ISO date parsing for "MMM dd"
  val dateString = expense.createdAt.toString()
  val month =
      try {
        val monthNum = dateString.substring(5, 7).toInt()
        val months =
            listOf(
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec",
            )
        months[monthNum - 1]
      } catch (_: Exception) {
        "May"
      }
  val day =
      try {
        dateString.substring(8, 10)
      } catch (_: Exception) {
        "26"
      }

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable(onClick = onClick)
              .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    // Date
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
      Text(
          text = month,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(text = day, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }

    Spacer(Modifier.width(12.dp))

    // Icon
    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    Spacer(Modifier.width(12.dp))

    // Title and Payer info
    Column(modifier = Modifier.weight(1f)) {
      Text(
          text = expense.title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
      )

      val payers = expense.participants.filter { it.paidShare > 0 }
      val payerText =
          when {
            payers.size > 1 -> "Multiple people paid IRR${expense.amount.formatAmount()}"
            payers.size == 1 -> {
              val payerId = payers[0].userId
              val payerMember = members.find { it.userId == payerId }
              val payerName =
                  when {
                    payerMember?.isCurrentUser == true -> "You"
                    payerMember != null -> payerMember.name
                    else -> "Someone"
                  }
              "$payerName paid IRR${expense.amount.formatAmount()}"
            }
            else -> "No one paid"
          }

      Text(
          text = payerText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // Status and Share Amount
    Column(horizontalAlignment = Alignment.End) {
      Text(text = statusText, style = MaterialTheme.typography.labelSmall, color = statusColor)
      Text(
          text = "IRR${displayAmount.formatAmount()}",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          color = statusColor,
      )
    }
  }
}

@Preview
@Composable
fun ExpenseItemPreview() {
  val members =
      listOf(
          Member("user-1", "You", "you@example.com", isCurrentUser = true),
          Member("user-2", "Ali B.", "ali@example.com"),
      )
  val expense =
      Expense(
          id = "1",
          householdId = "h1",
          title = "Pizza",
          amount = 30.0,
          creator = "user-1",
          createdAt = Instant.fromEpochMilliseconds(1716672000000L), // May 26, 2024
          participants =
              listOf(ParticipantShare("user-1", 30.0, 15.0), ParticipantShare("user-2", 0.0, 15.0)),
          splitMethod = com.opensplit.dto.expense.SplitMethod.Equally(listOf("user-1", "user-2")),
          syncStatus = com.opensplit.dto.expense.SyncStatus.SYNCED,
      )
  OpenSplitTheme {
    Surface {
      ExpenseItem(expense = expense, members = members, currentUserId = "user-1", onClick = {})
    }
  }
}
