package com.opensplit.features.auth

import com.arkivanov.decompose.router.stack.pushNew
import com.opensplit.component.CContext
import com.opensplit.component.navigation
import com.opensplit.root.ComponentProvider
import com.opensplit.root.Destination
import com.opensplit.root.DestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable


@Serializable
enum class AuthMode {
    SignIn,
    SignUp,
}

data class AuthViewState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val session: com.opensplit.dto.auth.AuthSessionState? = null,
    val isSubmitting: Boolean = false,
)

interface AuthComponent : Destination {
    val uiState: StateFlow<AuthViewState>
    fun useSignIn()
    fun useSignUp()
    fun updateEmail(email: String)
    fun updatePassword(password: String)
    suspend fun submit()

    @Serializable
    data class Config(
        val mode: AuthMode,
    ) : DestinationConfig {
        override fun createComponent(
            componentProvider: ComponentProvider,
            cContext: CContext
        ): Any {
            return componentProvider(AuthComponent::class, cContext, this)
        }
    }
}


interface ComponentFactory<Config, Component> {
    fun create(context: CContext, config: Config): Component
}

class DefaultAuthComponent(
    context: CContext,
    config: AuthComponent.Config,
    private val gateway: AuthGateway,
    private val tokenStorage: TokenStorage,
) : AuthComponent, CContext by context {
    private val _uiState = MutableStateFlow(AuthViewState(mode = config.mode))
    override val uiState: StateFlow<AuthViewState> = _uiState
    // apiCallScope is intentionally not used here: submit is suspend and runs on the
    // caller coroutine so callers/tests can await completion.

    override fun useSignIn() {
        _uiState.update {
            it.copy(
                mode = AuthMode.SignIn,
                fieldErrors = emptyMap(),
                generalError = null
            )
        }
    }

    override fun useSignUp() {
        navigation.pushNew(AuthComponent.Config(AuthMode.SignUp))
        _uiState.update {
            it.copy(
                mode = AuthMode.SignUp,
                fieldErrors = emptyMap(),
                generalError = null
            )
        }
    }

    override fun updateEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                fieldErrors = it.fieldErrors - "email",
                generalError = null
            )
        }
    }

    override fun updatePassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                fieldErrors = it.fieldErrors - "password",
                generalError = null
            )
        }
    }

    override suspend fun submit() {
        val current = _uiState.value
        // Run validations and network call on caller coroutine so tests can await completion
        val validation = when (current.mode) {
            AuthMode.SignIn -> com.opensplit.validation.auth.AuthValidation.validateSignIn(
                current.email,
                current.password
            )

            AuthMode.SignUp -> com.opensplit.validation.auth.AuthValidation.validateSignUp(
                current.email,
                current.password
            )
        }

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    fieldErrors = validation.errors,
                    generalError = null,
                    session = null,
                    isSubmitting = false
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                fieldErrors = emptyMap(),
                generalError = null,
                isSubmitting = true
            )
        }

        try {
            val result = when (current.mode) {
                AuthMode.SignIn -> gateway.signIn(current.email, current.password)
                AuthMode.SignUp -> gateway.signUp(current.email, current.password)
            }
            // Persist access token (best-effort). Swallow errors so persistence
            // problems don't prevent successful authentication from being reported.
            try {
                result.session.accessToken.let { token ->
                    tokenStorage.saveAccessToken(token)
                }
            } catch (_: Throwable) {
            }
            _uiState.update {
                it.copy(
                    session = result.session,
                    fieldErrors = emptyMap(),
                    generalError = null,
                    isSubmitting = false,
                )
            }
        } catch (e: AuthRemoteException) {
            _uiState.update {
                it.copy(
                    fieldErrors = e.fieldErrors,
                    generalError = e.generalError,
                    session = null,
                    isSubmitting = false,
                )
            }
        }
    }
}

class FakeAuthComponent(
    uiState: AuthViewState = AuthViewState(
        session = com.opensplit.dto.auth.AuthSessionState(
            userId = "user-1",
            email = "amir@example.com",
            accessToken = "token"
        )
    )
) : AuthComponent {
    private val _uiState = MutableStateFlow(uiState)
    override val uiState: StateFlow<AuthViewState> = _uiState
    override fun useSignIn() {}
    override fun useSignUp() {}
    override fun updateEmail(email: String) {}
    override fun updatePassword(password: String) {}
    override suspend fun submit() {}
}

