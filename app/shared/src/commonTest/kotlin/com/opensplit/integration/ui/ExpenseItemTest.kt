package com.opensplit.integration.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.opensplit.domain.FakeExpenseFactory
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.integration.expense.ExpenseItem
import com.opensplit.ui.OpenSplitTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ExpenseItemTest {

  @Test
  fun testPendingExpenseShowsPendingSync() = runComposeUiTest {
    setContent {
      OpenSplitTheme {
        ExpenseItem(
            expense = FakeExpenseFactory.create(syncStatus = SyncStatus.PENDING),
            members = FakeMemberFactory.createList(),
            currentUserId = "user-1",
            onClick = {},
        )
      }
    }

    onNode(hasText("Pending sync")).assertIsDisplayed()
  }

  @Test
  fun testSyncedExpenseShowsNoPendingSync() = runComposeUiTest {
    setContent {
      OpenSplitTheme {
        ExpenseItem(
            expense = FakeExpenseFactory.create(syncStatus = SyncStatus.SYNCED),
            members = FakeMemberFactory.createList(),
            currentUserId = "user-1",
            onClick = {},
        )
      }
    }

    onNode(hasText("Pending sync")).assertDoesNotExist()
  }
}
