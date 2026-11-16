package com.opensplit.integration.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.opensplit.domain.FakeGroupSettlementFactory
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.domain.FakeSettlementTransferFactory
import com.opensplit.integration.group.details.SettlementSection
import com.opensplit.ui.OpenSplitTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SettlementSectionTest {

  @Test
  fun testTransfersShowPayerPayeeAndAmount() = runComposeUiTest {
    setContent {
      OpenSplitTheme {
        SettlementSection(
            settlement = FakeGroupSettlementFactory.create(),
            members = FakeMemberFactory.createList(),
        )
      }
    }

    onNode(hasTestTag("settlement-section")).assertIsDisplayed()
    onNode(hasText("Settle up")).assertIsDisplayed()
    // user-2 (Abolqasem Ferdowsi) owes user-1 (the current user)
    onNode(hasText("Abolqasem Ferdowsi pays You")).assertIsDisplayed()
    onNode(hasText("IRR10.00")).assertIsDisplayed()
  }

  @Test
  fun testSettledGroupShowsAllSettledUp() = runComposeUiTest {
    setContent {
      OpenSplitTheme {
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

    onNode(hasText("All settled up")).assertIsDisplayed()
  }

  @Test
  fun testUnknownMemberFallsBackToSomeone() = runComposeUiTest {
    setContent {
      OpenSplitTheme {
        SettlementSection(
            settlement =
                FakeGroupSettlementFactory.create(
                    transfers =
                        listOf(
                            FakeSettlementTransferFactory.create(
                                fromUserId = "user-9",
                                toUserId = "user-1",
                                amount = 5.0,
                            ),
                        ),
                ),
            members = FakeMemberFactory.createList(),
        )
      }
    }

    onNode(hasText("Someone pays You")).assertIsDisplayed()
    onNode(hasText("IRR5.00")).assertIsDisplayed()
  }
}
