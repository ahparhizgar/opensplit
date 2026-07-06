package com.opensplit.e2e.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
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
  fun testLoginFlowHappyPath() = runComposeUiTest {
    val context = TestCContext()
    val koin = uiKoin()
    val root = koin.injectUiRoot(context)
    setContent { App(root) }

    onNode(hasTestTag("welcome-screen")).waitForExistence().assertExists()
    onNode(hasText("Log in", ignoreCase = true)).performClick()
    onNode(hasTestTag("login-screen")).assertExists()
    onNode(isFocused()).performTextInput("user1@example.com")
    onNode(isFocused()).performKeyInput { pressKey(Key.Tab) }
    onNode(isFocused()).performTextInput("password1234")
    onNode(isFocused()).performKeyInput { pressKey(Key.Enter) }
    onNode(hasTestTag("household-list")).waitForExistence().assertExists()
  }
}
