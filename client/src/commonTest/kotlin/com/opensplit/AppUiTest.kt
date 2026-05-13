package com.opensplit

import com.opensplit.features.auth.AuthController
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.AuthSubmissionResult
import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.features.auth.householdContextState
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class FakeAuthGateway : AuthGateway {
    var signUpCalls = 0

    override suspend fun signUp(email: String, password: String): AuthSubmissionResult {
        signUpCalls++
        return AuthSubmissionResult(
            session = AuthSessionState(userId = "user-1", email = email),
            householdContext = HouseholdContextState(
                authenticated = true,
                email = email,
                message = "Authenticated household context",
            ),
        )
    }

    override suspend fun signIn(email: String, password: String): AuthSubmissionResult = signUp(email, password)
}

class AppUiTest {
    @Test
    fun authControllerRoutesToHouseholdContextAfterValidSubmission() {
        val gateway = FakeAuthGateway()
        val controller = AuthController(gateway)

        controller.updateEmail("amir@example.com")
        controller.updatePassword("password123")
        runBlocking { controller.submit() }

        assertNotNull(controller.state.session)
        assertEquals("amir@example.com", controller.state.session?.email)
        assertEquals("Authenticated household context", controller.householdContextState()?.message)
        assertEquals(1, gateway.signUpCalls)
    }

    @Test
    fun authControllerShowsValidationErrorsForInvalidSubmission() {
        val controller = AuthController(FakeAuthGateway())

        controller.updateEmail("bad-email")
        controller.updatePassword("short")
        runBlocking { controller.submit() }

        assertNull(controller.state.session)
        assertEquals("Enter a valid email address", controller.state.fieldErrors["email"])
        assertEquals("Password must be at least 8 characters", controller.state.fieldErrors["password"])
    }
}
