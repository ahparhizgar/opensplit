package com.opensplit.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ServerMessagesTest {
    @Test
    fun returnsHealthText() {
        assertEquals("ok", ServerMessages.healthResponse())
    }
}
