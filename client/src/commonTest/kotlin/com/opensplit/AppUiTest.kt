package com.opensplit

import com.opensplit.features.auth.AuthController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AppUiTest {
    @Test
    fun authControllerRoutesToHouseholdContextAfterValidSubmission() {
        val controller = AuthController()

        controller.updateEmail("amir@example.com")
        controller.updatePassword("password123")
        controller.submit()

        assertNotNull(controller.state.session)
        assertEquals("amir@example.com", controller.state.session?.email)
        assertEquals("Authenticated household context", controller.householdContextState()?.message)
    }

    @Test
    fun authControllerShowsValidationErrorsForInvalidSubmission() {
        val controller = AuthController()

        controller.updateEmail("bad-email")
        controller.updatePassword("short")
        controller.submit()

        assertNull(controller.state.session)
        assertEquals("Enter a valid email address", controller.state.fieldErrors["email"])
        assertEquals("Password must be at least 8 characters", controller.state.fieldErrors["password"])
    }
}
