package com.opensplit

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.component.DefaultCContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
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
        session = AuthSessionState(userId = "user-2", email = email, accessToken = "token-user-2-$email"),
        householdContext = HouseholdContextState(authenticated = true, email = email, message = "Authenticated household context"),
    )

    override suspend fun signIn(email: String, password: String): AuthSubmissionResult = signUp(email, password)
}

class ComposeAppCommonTest {
    @Test
    fun authControllerUsesSharedValidationAndRoutesOnSuccess() {
        val lifecycle = LifecycleRegistry()
        val cctx = DefaultCContext(lifecycle = lifecycle)
        val component = DefaultAuthComponent.Factory(FakeGateway()).create(cctx)

        component.useSignUp()
        component.updateEmail("bad-email")
        component.updatePassword("short")
        runBlocking { component.submit() }

        val state1 = component.uiState.value
        assertTrue(state1.fieldErrors.isNotEmpty())
        assertEquals(AuthMode.SignUp, state1.mode)
        assertFalse(state1.session != null)

        component.updateEmail("valid@example.com")
        component.updatePassword("password123")
        runBlocking { component.submit() }

        val state2 = component.uiState.value
        assertTrue(state2.fieldErrors.isEmpty())
        assertEquals("valid@example.com", state2.session?.email)
    }
}
