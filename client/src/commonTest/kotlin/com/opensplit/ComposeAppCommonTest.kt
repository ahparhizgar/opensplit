package com.opensplit

import com.opensplit.features.auth.AuthController
import com.opensplit.features.auth.AuthMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposeAppCommonTest {

    @Test
    fun authControllerUsesSharedValidationAndRoutesOnSuccess() {
        val controller = AuthController()

        controller.useSignUp()
        controller.updateEmail("bad-email")
        controller.updatePassword("short")
        controller.submit()

        assertTrue(controller.state.fieldErrors.isNotEmpty())
        assertEquals(AuthMode.SignUp, controller.state.mode)
        assertFalse(controller.state.session != null)

        controller.updateEmail("valid@example.com")
        controller.updatePassword("password123")
        controller.submit()

        assertTrue(controller.state.fieldErrors.isEmpty())
        assertEquals("valid@example.com", controller.state.session?.email)
    }
}
