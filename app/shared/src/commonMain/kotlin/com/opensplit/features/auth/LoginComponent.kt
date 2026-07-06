package com.opensplit.features.auth

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.remote.RemoteException
import com.opensplit.validation.auth.AuthValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface LoginComponent {
  val state: StateFlow<LoginViewState>

  fun onEmailChanged(email: String)

  fun onPasswordChanged(password: String)

  fun onLoginClicked()

  fun onForgotPasswordClicked()

  fun onBackClicked()

  interface Factory {
    fun create(
        context: CContext,
        navigation: StackNavigation<AuthConfig>,
        onAuthenticated: () -> Unit,
    ): LoginComponent
  }
}

data class LoginViewState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
)

class DefaultLoginComponent(
    private val context: CContext,
    private val navigation: StackNavigation<AuthConfig>,
    private val gateway: AuthGateway,
    private val tokenStorage: TokenStorage,
    private val onAuthenticated: () -> Unit,
) : LoginComponent {
  private val scope = context.componentScope()
  private val _state = MutableStateFlow(LoginViewState())
  override val state: StateFlow<LoginViewState> = _state

  override fun onEmailChanged(email: String) {
    _state.update {
      it.copy(email = email, fieldErrors = it.fieldErrors - "email", generalError = null)
    }
  }

  override fun onPasswordChanged(password: String) {
    _state.update {
      it.copy(password = password, fieldErrors = it.fieldErrors - "password", generalError = null)
    }
  }

  override fun onLoginClicked() {
    val current = _state.value
    val validation = AuthValidation.validateSignIn(current.email, current.password)
    if (!validation.isValid) {
      _state.update { it.copy(fieldErrors = validation.errors, isSubmitting = false) }
      return
    }

    _state.update { it.copy(isSubmitting = true, fieldErrors = emptyMap(), generalError = null) }

    scope.launch {
      try {
        val result = gateway.signIn(current.email, current.password)
        try {
          tokenStorage.saveAccessToken(result.session.accessToken)
        } catch (_: Throwable) {}
        onAuthenticated()
      } catch (e: RemoteException) {
        _state.update {
          it.copy(fieldErrors = e.fieldErrors, generalError = e.generalError, isSubmitting = false)
        }
      } catch (e: Exception) {
        _state.update { it.copy(generalError = e.message ?: "Unknown error", isSubmitting = false) }
      }
    }
  }

  override fun onForgotPasswordClicked() {
    navigation.pushNew(AuthConfig.ResetPassword)
  }

  override fun onBackClicked() {
    navigation.pop()
  }

  class Factory(
      private val gateway: AuthGateway,
      private val tokenStorage: TokenStorage,
  ) : LoginComponent.Factory {
    override fun create(
        context: CContext,
        navigation: StackNavigation<AuthConfig>,
        onAuthenticated: () -> Unit,
    ): LoginComponent =
        DefaultLoginComponent(context, navigation, gateway, tokenStorage, onAuthenticated)
  }
}

class FakeLoginComponent(state: LoginViewState = LoginViewState()) : LoginComponent {
  override val state: StateFlow<LoginViewState> = MutableStateFlow(state)

  override fun onEmailChanged(email: String) {}

  override fun onPasswordChanged(password: String) {}

  override fun onLoginClicked() {}

  override fun onForgotPasswordClicked() {}

  override fun onBackClicked() {}

  class Factory : LoginComponent.Factory {
    override fun create(
        context: CContext,
        navigation: StackNavigation<AuthConfig>,
        onAuthenticated: () -> Unit,
    ): LoginComponent = FakeLoginComponent()
  }
}
