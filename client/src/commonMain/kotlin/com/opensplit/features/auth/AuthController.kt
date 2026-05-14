package com.opensplit.features.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.validation.auth.AuthValidation
import com.ahparhizgar.apicallerror.ApiCallError

enum class AuthMode {
    SignIn,
    SignUp,
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val session: AuthSessionState? = null,
    val isSubmitting: Boolean = false,
)

class AuthController(
    private val gateway: AuthGateway = createAuthGateway(),
) {
    var state by mutableStateOf(AuthUiState())
        private set

    fun useSignIn() {
        state = state.copy(mode = AuthMode.SignIn, fieldErrors = emptyMap(), generalError = null)
    }

    fun useSignUp() {
        state = state.copy(mode = AuthMode.SignUp, fieldErrors = emptyMap(), generalError = null)
    }

    fun updateEmail(email: String) {
        state = state.copy(email = email, fieldErrors = state.fieldErrors - "email", generalError = null)
    }

    fun updatePassword(password: String) {
        state = state.copy(password = password, fieldErrors = state.fieldErrors - "password", generalError = null)
    }

    suspend fun submit() {
        val validation = when (state.mode) {
            AuthMode.SignIn -> AuthValidation.validateSignIn(state.email, state.password)
            AuthMode.SignUp -> AuthValidation.validateSignUp(state.email, state.password)
        }

        if (!validation.isValid) {
            state = state.copy(fieldErrors = validation.errors, generalError = null, session = null, isSubmitting = false)
            return
        }

        state = state.copy(fieldErrors = emptyMap(), generalError = null, isSubmitting = true)

        try {
            val result = when (state.mode) {
                AuthMode.SignIn -> gateway.signIn(state.email, state.password)
                AuthMode.SignUp -> gateway.signUp(state.email, state.password)
            }
            state = state.copy(
                session = result.session,
                fieldErrors = emptyMap(),
                generalError = null,
                isSubmitting = false,
            )
        } catch (error: AuthRemoteException) {
            state = state.copy(
                fieldErrors = error.fieldErrors,
                generalError = error.generalError,
                session = null,
                isSubmitting = false,
            )
        } catch (error: ApiCallError) {
            // Convert ApiCallError to user-facing message when possible
            val message = error.message ?: "Request failed"
            state = state.copy(
                fieldErrors = emptyMap(),
                generalError = message,
                session = null,
                isSubmitting = false,
            )
        }
    }
}

data class HouseholdContextState(
    val message: String,
    val email: String,
    val householdId: String? = null,
)

fun AuthController.householdContextState(): HouseholdContextState? {
    val session = state.session ?: return null
    return HouseholdContextState(
        message = "Authenticated household context",
        email = session.email,
        householdId = session.householdId,
    )
}
