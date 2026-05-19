package com.opensplit.dto.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthDtosTest {
    @Test
    fun dataClassesRemainStableAndCopyable() {
        val request = SignUpRequest(email = "new@example.com", password = "password123")

        assertEquals("new@example.com", request.copy().email)
        assertEquals("password123", request.password)
    }
}
