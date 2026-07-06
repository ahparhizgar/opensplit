package com.opensplit.features.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun AuthRootScreen(
    component: AuthComponent,
    modifier: Modifier = Modifier,
) {
  Children(
      stack = component.stack,
      modifier = modifier,
      animation =
          predictiveBackAnimation(
              backHandler = component.backHandler,
              onBack = component::onBack,
              fallbackAnimation = stackAnimation(fade() + scale()),
          ),
  ) {
    when (val child = it.instance) {
      is AuthComponent.Child.Welcome ->
          WelcomeScreen(
              component = child.component,
              modifier = Modifier.testTag("welcome-screen"),
          )
      is AuthComponent.Child.Login ->
          LoginScreen(component = child.component, modifier = Modifier.testTag("login-screen"))
      is AuthComponent.Child.SignUp ->
          SignUpScreen(component = child.component, modifier = Modifier.testTag("sign-up-screen"))
      is AuthComponent.Child.ResetPassword ->
          ResetPasswordScreen(
              component = child.component,
              modifier = Modifier.testTag("reset-password-screen"),
          )
    }
  }
}
