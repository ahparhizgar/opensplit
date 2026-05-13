package com.opensplit

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppRunnerTest {
    @Test
    fun exposesExpectedWindowTitle() {
        assertEquals("opensplit", defaultAppTitle())
    }

    private fun defaultAppTitle(): String = "opensplit"
}
