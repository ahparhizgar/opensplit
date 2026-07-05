package com.opensplit.features.auth

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.component.navigation
import com.opensplit.features.household.my.MyHouseholdsListComponent
import com.opensplit.root.Destination
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthConfig {
  @Serializable data object Welcome : AuthConfig

  @Serializable data object Login : AuthConfig

  @Serializable data object SignUp : AuthConfig

  @Serializable data object ResetPassword : AuthConfig
}

interface AuthComponent : Destination {
  val stack: Value<ChildStack<AuthConfig, Child>>

  sealed class Child {
    class Welcome(val component: WelcomeComponent) : Child()

    class Login(val component: LoginComponent) : Child()

    class SignUp(val component: SignUpComponent) : Child()

    class ResetPassword(val component: ResetPasswordComponent) : Child()
  }

  @Serializable data object Config : TopLevelDestinationConfig

  interface Factory {
    fun create(cContext: CContext): AuthComponent
  }
}

class DefaultAuthComponent(
    context: CContext,
    private val welcomeFactory: WelcomeComponent.Factory,
    private val loginFactory: LoginComponent.Factory,
    private val signUpFactory: SignUpComponent.Factory,
    private val resetPasswordFactory: ResetPasswordComponent.Factory,
) : AuthComponent, CContext by context {

  private val navigation = StackNavigation<AuthConfig>()

  override val stack: Value<ChildStack<AuthConfig, AuthComponent.Child>> =
      childStack(
          source = navigation,
          serializer = null,
          initialConfiguration = AuthConfig.Welcome,
          handleBackButton = true,
          childFactory = ::createChild,
      )

  private fun createChild(config: AuthConfig, context: CContext): AuthComponent.Child {
    return when (config) {
      AuthConfig.Welcome -> AuthComponent.Child.Welcome(welcomeFactory.create(navigation))
      AuthConfig.Login ->
          AuthComponent.Child.Login(loginFactory.create(context, navigation, ::onAuthenticated))
      AuthConfig.SignUp ->
          AuthComponent.Child.SignUp(signUpFactory.create(context, navigation, ::onAuthenticated))
      AuthConfig.ResetPassword ->
          AuthComponent.Child.ResetPassword(resetPasswordFactory.create(navigation))
    }
  }

  private fun onAuthenticated() {
    (this as CContext).navigation.replaceCurrent(MyHouseholdsListComponent.Config)
  }

  class Factory(
      private val welcomeFactory: WelcomeComponent.Factory,
      private val loginFactory: LoginComponent.Factory,
      private val signUpFactory: SignUpComponent.Factory,
      private val resetPasswordFactory: ResetPasswordComponent.Factory,
  ) : AuthComponent.Factory {
    override fun create(cContext: CContext): AuthComponent =
        DefaultAuthComponent(
            cContext,
            welcomeFactory,
            loginFactory,
            signUpFactory,
            resetPasswordFactory,
        )
  }
}

class FakeAuthComponent(
    override val stack: Value<ChildStack<AuthConfig, AuthComponent.Child>> =
        MutableValue(
            ChildStack(
                active =
                    com.arkivanov.decompose.Child.Created(
                        AuthConfig.Welcome,
                        AuthComponent.Child.Welcome(FakeWelcomeComponent()),
                    ),
                backStack = emptyList(),
            )
        )
) : AuthComponent {
  class Factory : AuthComponent.Factory {
    override fun create(cContext: CContext): AuthComponent = FakeAuthComponent()
  }
}
