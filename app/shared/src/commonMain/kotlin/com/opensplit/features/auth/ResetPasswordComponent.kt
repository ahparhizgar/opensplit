package com.opensplit.features.auth

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ResetPasswordComponent {
  val state: StateFlow<ResetPasswordViewState>

  fun onEmailChanged(email: String)

  fun onPhoneChanged(phone: String)

  fun onBackClicked()

  fun onResetClicked()

  fun onTabChanged(tab: ResetPasswordTab)

  interface Factory {
    fun create(navigation: StackNavigation<AuthConfig>): ResetPasswordComponent
  }
}

data class ResetPasswordViewState(
    val email: String = "",
    val phoneNumber: String = "",
    val selectedTab: ResetPasswordTab = ResetPasswordTab.Email,
    val isSubmitting: Boolean = false,
)

enum class ResetPasswordTab {
  Email,
  Phone,
}

class DefaultResetPasswordComponent(private val navigation: StackNavigation<AuthConfig>) :
    ResetPasswordComponent {
  override val state: StateFlow<ResetPasswordViewState> = MutableStateFlow(ResetPasswordViewState())

  override fun onEmailChanged(email: String) {
    TODO()
  }

  override fun onPhoneChanged(phone: String) {
    TODO()
  }

  override fun onBackClicked() {
    navigation.pop()
  }

  override fun onResetClicked() {
    TODO()
  }

  override fun onTabChanged(tab: ResetPasswordTab) {
    TODO()
  }

  class Factory : ResetPasswordComponent.Factory {
    override fun create(navigation: StackNavigation<AuthConfig>): ResetPasswordComponent =
        DefaultResetPasswordComponent(navigation)
  }
}

class FakeResetPasswordComponent(state: ResetPasswordViewState = ResetPasswordViewState()) :
    ResetPasswordComponent {
  override val state: StateFlow<ResetPasswordViewState> = MutableStateFlow(state)

  override fun onEmailChanged(email: String) {}

  override fun onPhoneChanged(phone: String) {}

  override fun onBackClicked() {}

  override fun onResetClicked() {}

  override fun onTabChanged(tab: ResetPasswordTab) {}

  class Factory : ResetPasswordComponent.Factory {
    override fun create(navigation: StackNavigation<AuthConfig>): ResetPasswordComponent =
        FakeResetPasswordComponent()
  }
}
