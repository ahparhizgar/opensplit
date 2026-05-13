package com.opensplit

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

class AppUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun appLaunchesAndRespondsToClick() = runComposeUiTest {
        setContent { App() }

        onNodeWithText("Click me!").assertIsDisplayed()
        onNodeWithText("Click me!").performClick()
        onNodeWithTag("app-greeting").assertIsDisplayed()
    }
}
