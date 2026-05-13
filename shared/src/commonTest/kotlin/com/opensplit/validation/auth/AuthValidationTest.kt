package com.opensplit.validation.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthValidationTest {
    @Test
    fun acceptsValidCredentials() {
        val result = AuthValidation.validateSignUp("amir@example.com", "password123")

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun rejectsMalformedEmailAndShortPassword() {
        val result = AuthValidation.validateSignIn("bad-email", "short")

        assertFalse(result.isValid)
        assertEquals("Enter a valid email address", result.errors["email"])
        assertEquals("Password must be at least 8 characters", result.errors["password"])
    }
}
