package com.opensplit.e2e.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasRequestFocusAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.opensplit.App
import com.opensplit.component.TestCContext
import com.opensplit.util.injectUiRoot
import com.opensplit.util.uiKoin
import com.opensplit.util.waitForExistence
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AuthUiTest {
  @Test
  fun testAuthUi() = runComposeUiTest {
    val context = TestCContext()
    val koin = uiKoin()
    val root = koin.injectUiRoot(context)
    setContent { App(root) }

    onNode(hasTestTag("welcome-screen")).waitForExistence().assertExists()
    onNode(hasText("Log in", ignoreCase = true)).performClick()
    onNode(hasTestTag("login-screen")).assertExists()
    onNode(isFocusable())
  }
}
