package com.opensplit

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.features.auth.AuthController
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.AuthMode
import com.opensplit.features.auth.AuthSubmissionResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeGateway : AuthGateway {
    override suspend fun signUp(email: String, password: String): AuthSubmissionResult = AuthSubmissionResult(
        session = AuthSessionState(userId = "user-2", email = email),
        householdContext = HouseholdContextState(authenticated = true, email = email, message = "Authenticated household context"),
    )

    override suspend fun signIn(email: String, password: String): AuthSubmissionResult = signUp(email, password)
}

class ComposeAppCommonTest {
    @Test
    fun authControllerUsesSharedValidationAndRoutesOnSuccess() {
        val controller = AuthController(FakeGateway())

        controller.useSignUp()
        controller.updateEmail("bad-email")
        controller.updatePassword("short")
        runBlocking { controller.submit() }

        assertTrue(controller.state.fieldErrors.isNotEmpty())
        assertEquals(AuthMode.SignUp, controller.state.mode)
        assertFalse(controller.state.session != null)

        controller.updateEmail("valid@example.com")
        controller.updatePassword("password123")
        runBlocking { controller.submit() }

        assertTrue(controller.state.fieldErrors.isEmpty())
        assertEquals("valid@example.com", controller.state.session?.email)
    }
}
