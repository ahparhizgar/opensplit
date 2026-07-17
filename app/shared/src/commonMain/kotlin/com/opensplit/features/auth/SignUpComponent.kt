package com.opensplit.features.auth

import com.ahparhizgar.katch.ApiCallError
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.remote.fieldErrors
import com.opensplit.remote.userMessage
import com.opensplit.validation.auth.AuthValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface SignUpComponent {
  val state: StateFlow<SignUpViewState>

  fun onFullNameChanged(name: String)

  fun onEmailChanged(email: String)

  fun onPasswordChanged(password: String)

  fun onPhoneChanged(phone: String)

  fun onBackClicked()

  fun onDoneClicked()

  interface Factory {
    fun create(
        context: CContext,
        navigation: StackNavigation<AuthConfig>,
        onAuthenticated: () -> Unit,
    ): SignUpComponent
  }
}

data class SignUpViewState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val isSubmitting: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
)

class DefaultSignUpComponent(
    private val context: CContext,
    private val navigation: StackNavigation<AuthConfig>,
    private val gateway: AuthApi,
    private val tokenStorage: TokenStorage,
    private val onAuthenticated: () -> Unit,
) : SignUpComponent {
  private val scope = context.componentScope()
  private val _state = MutableStateFlow(SignUpViewState())
  override val state: StateFlow<SignUpViewState> = _state

  override fun onFullNameChanged(name: String) {
    _state.update { it.copy(fullName = name, fieldErrors = it.fieldErrors - "fullName") }
  }

  override fun onEmailChanged(email: String) {
    _state.update { it.copy(email = email, fieldErrors = it.fieldErrors - "email") }
  }

  override fun onPasswordChanged(password: String) {
    _state.update { it.copy(password = password, fieldErrors = it.fieldErrors - "password") }
  }

  override fun onPhoneChanged(phone: String) {
    _state.update { it.copy(phoneNumber = phone) }
  }

  override fun onBackClicked() {
    navigation.pop()
  }

  override fun onDoneClicked() {
    val current = _state.value
    val validation = AuthValidation.validateSignUp(current.email, current.password)
    if (!validation.isValid) {
      _state.update { it.copy(fieldErrors = validation.errors, isSubmitting = false) }
      return
    }

    _state.update { it.copy(isSubmitting = true, fieldErrors = emptyMap(), generalError = null) }

    scope.launch {
      try {
        val result = gateway.signUp(current.email, current.password)
        tokenStorage.saveAccessToken(result.session.accessToken)
        onAuthenticated()
      } catch (e: ApiCallError) {
        _state.update {
          it.copy(fieldErrors = e.fieldErrors, generalError = e.userMessage, isSubmitting = false)
        }
      }
    }
  }

  class Factory(
      private val gateway: AuthApi,
      private val tokenStorage: TokenStorage,
  ) : SignUpComponent.Factory {
    override fun create(
        context: CContext,
        navigation: StackNavigation<AuthConfig>,
        onAuthenticated: () -> Unit,
    ): SignUpComponent =
        DefaultSignUpComponent(context, navigation, gateway, tokenStorage, onAuthenticated)
  }
}

class FakeSignUpComponent(state: SignUpViewState = SignUpViewState()) : SignUpComponent {
  override val state: StateFlow<SignUpViewState> = MutableStateFlow(state)

  override fun onFullNameChanged(name: String) {}

  override fun onEmailChanged(email: String) {}

  override fun onPasswordChanged(password: String) {}

  override fun onPhoneChanged(phone: String) {}

  override fun onBackClicked() {}

  override fun onDoneClicked() {}

  class Factory : SignUpComponent.Factory {
    override fun create(
        context: CContext,
        navigation: StackNavigation<AuthConfig>,
        onAuthenticated: () -> Unit,
    ): SignUpComponent = FakeSignUpComponent()
  }
}
