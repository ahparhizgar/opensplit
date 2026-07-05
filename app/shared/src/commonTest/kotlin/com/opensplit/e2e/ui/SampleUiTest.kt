package com.opensplit.e2e.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SampleUiTest {
  @Test fun testBuyMilk() = runComposeUiTest { setContent {} }
}
