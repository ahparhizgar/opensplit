package com.opensplit

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.opensplit.component.DefaultCContext
import com.opensplit.features.auth.AuthMode
import com.opensplit.features.auth.DefaultAuthComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthComponentTest {
    @Test
    fun authControllerUsesSharedValidationAndRoutesOnSuccess() = runTest {
        val lifecycle = LifecycleRegistry()
        val cctx = DefaultCContext(lifecycle = lifecycle)
        val component = DefaultAuthComponent.Factory(FakeAuthGateway()).create(cctx)

        component.useSignUp()
        component.updateEmail("bad-email")
        component.updatePassword("short")
        component.submit()

        component.uiState.value.let { state ->
            assertTrue(state.fieldErrors.isNotEmpty())
            assertEquals(AuthMode.SignUp, state.mode)
            assertFalse(state.session != null)
        }

        component.updateEmail("valid@example.com")
        component.updatePassword("password123")
        component.submit()

        component.uiState.value.let { state ->
            assertTrue(state.fieldErrors.isEmpty())
            assertEquals("valid@example.com", state.session?.email)
        }
    }

    @Test
    fun authControllerRoutesToHouseholdContextAfterValidSubmission() = runTest {
        val gateway = FakeAuthGateway()
        val lifecycle = LifecycleRegistry()
        val cctx = DefaultCContext(lifecycle = lifecycle)
        val component = DefaultAuthComponent.Factory(gateway).create(cctx)

        component.updateEmail("amir@example.com")
        component.updatePassword("password123")
        component.submit()

        val state = component.uiState.value
        assertNotNull(state.session)
        assertEquals("amir@example.com", state.session.email)
        assertEquals(1, gateway.signUpCalls)
    }
}
