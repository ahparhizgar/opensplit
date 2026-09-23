package com.opensplit.e2e.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import com.opensplit.App
import com.opensplit.component.TestCContext
import com.opensplit.util.injectUiRoot
import com.opensplit.util.uiKoin
import com.opensplit.util.waitForExistence
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AddExpenseUiTest {

  private suspend fun ComposeUiTest.loginAndGoToAddExpense() {
    val context = TestCContext()
    val koin = uiKoin()
    val root = koin.injectUiRoot(context)
    setContent { App(root) }

    onNode(hasTestTag("welcome-screen")).waitForExistence().assertExists()
    onNode(hasText("Log in", ignoreCase = true)).performClick()
    onNode(hasTestTag("login-screen")).assertExists()
    onNode(isFocused()).performTextInput("user1@example.com")
    onNode(isRoot()).performKeyInput { pressKey(Key.Tab) }
    onNode(isFocused()).performTextInput("password1234")
    onNode(isRoot()).performKeyInput { pressKey(Key.Enter) }
    onNode(hasTestTag("group-list")).waitForExistence().assertExists()

    // Click on the first group
    onNode(hasTestTag("show-settled-btn")).waitForExistence().performClick()
    onNode(hasText("Maple House")).waitForExistence().performClick()

    // Click Add Expense (FAB)
    onNode(hasContentDescription("Add Expense", ignoreCase = true))
        .waitForExistence()
        .performClick()

    // Wait for Add Expense Screen
    onNode(hasTestTag("expense-amount")).waitForExistence().assertExists()
  }

  @Test
  fun testEqualSplit() = runComposeUiTest {
    loginAndGoToAddExpense()

    // Enter amount
    onNode(hasTestTag("expense-amount")).performTextInput("100")
    onNode(isRoot()).performKeyInput { pressKey(Key.Tab) }

    // Enter description
    onNode(hasTestTag("expense-description")).performTextInput("Dinner")

    // Select "Split equally" (though it's default, we can just save it or explicitly verify)
    // Verify equal split summary button exists
    onNode(hasText("owes", substring = true)).assertExists()

    // Save
    onNode(hasContentDescription("Save", ignoreCase = true)).performClick()

    // Wait for the expense to appear (we don't mock it completely, but in a fake DB it works)
    onNode(hasText("Dinner")).waitForExistence().assertExists()
  }

  @Test
  fun testExactAmounts() = runComposeUiTest {
    loginAndGoToAddExpense()

    // Enter amount
    onNode(hasTestTag("expense-amount")).performTextInput("100")

    // Enter description
    onNode(hasTestTag("expense-description")).performTextInput("Lunch")

    // Click on quick split button to change
    onNode(hasText("owes", substring = true)).performClick()

    // Wait for Quick Split Screen, click More Options
    onNode(hasText("More options")).waitForExistence().performClick()

    // Wait for Adjust Split Screen (MoreSplitOptionsScreen)
    onNode(hasText("Unequally", ignoreCase = true)).waitForExistence().performClick()

    // In UnequallySplitPage, we have text fields with exact-amount-<id>
    onNode(hasTestTag("exact-amount-user-1")).performTextReplacement("60")
    onNode(hasTestTag("exact-amount-user-2")).performTextReplacement("40")

    // Click Done
    onNode(hasContentDescription("Done", ignoreCase = true)).performClick()

    // Save
    onNode(hasContentDescription("Save", ignoreCase = true)).performClick()

    onNode(hasText("Lunch")).waitForExistence().assertExists()
  }

  @Test
  fun testPercentageSplit() = runComposeUiTest {
    loginAndGoToAddExpense()

    // Enter amount
    onNode(hasTestTag("expense-amount")).performTextInput("100")

    // Enter description
    onNode(hasTestTag("expense-description")).performTextInput("Taxi")

    // Click on quick split button to change
    onNode(hasText("owes", substring = true)).performClick()

    // Wait for Quick Split Screen, click More Options
    onNode(hasText("More options")).waitForExistence().performClick()

    // Wait for Adjust Split Screen
    onNode(hasText("Percentage", ignoreCase = true)).waitForExistence().performClick()

    // In PercentageSplitPage
    onNode(hasTestTag("percentage-amount-user-1")).performTextReplacement("60")
    onNode(hasTestTag("percentage-amount-user-2")).performTextReplacement("40")

    // Click Done
    onNode(hasContentDescription("Done", ignoreCase = true)).performClick()

    // Save
    onNode(hasContentDescription("Save", ignoreCase = true)).performClick()

    onNode(hasText("Taxi")).waitForExistence().assertExists()
  }

  @Test
  fun testMultiplePayers() = runComposeUiTest {
    loginAndGoToAddExpense()

    // Enter amount
    onNode(hasTestTag("expense-amount")).performTextInput("100")
    onNode(isRoot()).performKeyInput { pressKey(Key.Tab) }

    // Enter description
    onNode(hasTestTag("expense-description")).performTextInput("Groceries")

    // Click quick split button to change
    onNode(hasText("owes", substring = true)).performClick()

    // Wait for Quick Split Screen, click More Options
    onNode(hasText("More options")).waitForExistence().performClick()

    // On MoreSplitOptionsScreen, click "Paid by ..."
    onNode(hasText("Paid by", substring = true)).performClick()

    // Click "Multiple people"
    onNode(hasText("Multiple people")).waitForExistence().performClick()

    // Now in PaidAmountsScreen
    onNode(hasTestTag("paid-amount-user-1")).performTextReplacement("70")
    onNode(hasTestTag("paid-amount-user-2")).performTextReplacement("30")

    // Click Done on PaidAmountsScreen -> returns to Main Expense Form
    onNode(hasContentDescription("Done", ignoreCase = true)).waitForExistence().performClick()

    // Amount should be updated on the main screen to 100.00
    onNode(hasText("100.00", substring = true)).assertExists()

    // It's split equally by default
    onNode(hasText("equally")).assertExists()

    // Save
    onNode(hasContentDescription("Save", ignoreCase = true)).performClick()

    onNode(hasText("Groceries")).waitForExistence().assertExists()
  }
}
