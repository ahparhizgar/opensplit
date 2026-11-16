package com.opensplit.integration.group.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensplit.domain.FakeGroupSettlementFactory
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.domain.GroupSettlement
import com.opensplit.domain.Member
import com.opensplit.domain.SettlementTransfer
import com.opensplit.ui.ComponentPreview
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.util.formatAmount

/**
 * Simplified settle-up transfers for a group: who pays whom and how much, derived from the
 * group's expenses. Shown at the top of the group details screen.
 */
@Composable
fun SettlementSection(
    settlement: GroupSettlement,
    members: List<Member>,
    modifier: Modifier = Modifier,
) {
  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("settlement-section"),
  ) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
          text = "Settle up",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
      )
      if (settlement.isSettled) {
        Text(
            text = "All settled up",
            style = MaterialTheme.typography.bodyMedium,
        )
      } else {
        settlement.transfers.forEach { transfer ->
          SettlementTransferRow(transfer = transfer, members = members)
        }
      }
    }
  }
}

@Composable
private fun SettlementTransferRow(
    transfer: SettlementTransfer,
    members: List<Member>,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
        text =
            "${memberDisplayName(members, transfer.fromUserId)} pays " +
                memberDisplayName(members, transfer.toUserId),
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = "IRR${transfer.amount.formatAmount()}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
    )
  }
}

private fun memberDisplayName(members: List<Member>, userId: String): String {
  val member = members.find { it.userId == userId }
  return when {
    member?.isCurrentUser == true -> "You"
    member != null -> member.name
    else -> "Someone"
  }
}

@ComponentPreview
@Composable
fun SettlementSectionPreview() {
  OpenSplitTheme {
    Surface {
      SettlementSection(
          settlement = FakeGroupSettlementFactory.create(),
          members = FakeMemberFactory.createList(),
      )
    }
  }
}

@ComponentPreview
@Composable
fun SettlementSectionSettledPreview() {
  OpenSplitTheme {
    Surface {
      SettlementSection(
          settlement =
              FakeGroupSettlementFactory.create(
                  balances = emptyList(),
                  transfers = emptyList(),
              ),
          members = FakeMemberFactory.createList(),
      )
    }
  }
}
