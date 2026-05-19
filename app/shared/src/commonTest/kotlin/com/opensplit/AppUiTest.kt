package com.opensplit

import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.component.DefaultCContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.AuthSubmissionResult
import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.HouseholdContextState
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
            session = AuthSessionState(userId = "user-1", email = email, accessToken = "token-user-1-$email"),
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
        val lifecycle = LifecycleRegistry()
        val cctx = DefaultCContext(lifecycle = lifecycle)
        val component = DefaultAuthComponent.Factory(gateway).create(cctx)

        component.updateEmail("amir@example.com")
        component.updatePassword("password123")
        runBlocking { component.submit() }

        val state = component.uiState.value
        assertNotNull(state.session)
        assertEquals("amir@example.com", state.session?.email)
        assertEquals(1, gateway.signUpCalls)
    }

    @Test
    fun authControllerShowsValidationErrorsForInvalidSubmission() {
        val lifecycle = LifecycleRegistry()
        val cctx = DefaultCContext(lifecycle = lifecycle)
        val component = DefaultAuthComponent.Factory(FakeAuthGateway()).create(cctx)

        component.updateEmail("bad-email")
        component.updatePassword("short")
        runBlocking { component.submit() }

        val state = component.uiState.value
        assertNull(state.session)
        assertEquals("Enter a valid email address", state.fieldErrors["email"])
        assertEquals("Password must be at least 8 characters", state.fieldErrors["password"])
    }
}
