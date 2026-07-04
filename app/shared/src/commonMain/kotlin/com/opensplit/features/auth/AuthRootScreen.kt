package com.opensplit.features.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

@Composable
fun AuthRootScreen(
    component: AuthComponent,
    modifier: Modifier = Modifier,
) {
  Children(stack = component.stack, modifier = modifier, animation = stackAnimation(fade())) {
    when (val child = it.instance) {
      is AuthComponent.Child.Welcome -> WelcomeScreen(child.component)
      is AuthComponent.Child.Login -> LoginScreen(child.component)
      is AuthComponent.Child.SignUp -> SignUpScreen(child.component)
      is AuthComponent.Child.ResetPassword -> ResetPasswordScreen(child.component)
    }
  }
}
