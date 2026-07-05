package com.opensplit.features.auth

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew

interface WelcomeComponent {
  fun onSignUpClicked()

  fun onLoginClicked()

  interface Factory {
    fun create(navigation: StackNavigation<AuthConfig>): WelcomeComponent
  }
}

class DefaultWelcomeComponent(private val navigation: StackNavigation<AuthConfig>) :
    WelcomeComponent {
  override fun onSignUpClicked() {
    navigation.pushNew(AuthConfig.SignUp)
  }

  override fun onLoginClicked() {
    navigation.pushNew(AuthConfig.Login)
  }

  class Factory : WelcomeComponent.Factory {
    override fun create(navigation: StackNavigation<AuthConfig>): WelcomeComponent =
        DefaultWelcomeComponent(navigation)
  }
}

class FakeWelcomeComponent : WelcomeComponent {
  override fun onSignUpClicked() {}

  override fun onLoginClicked() {}

  class Factory : WelcomeComponent.Factory {
    override fun create(navigation: StackNavigation<AuthConfig>): WelcomeComponent =
        FakeWelcomeComponent()
  }
}
