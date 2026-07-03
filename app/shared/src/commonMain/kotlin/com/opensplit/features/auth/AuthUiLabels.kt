package com.opensplit.features.auth

fun authSubmitLabel(mode: AuthMode): String =
    when (mode) {
      AuthMode.SignIn -> "Sign in"
      AuthMode.SignUp -> "Create account"
    }

fun authToggleLabel(mode: AuthMode): String =
    when (mode) {
      AuthMode.SignIn -> "Need an account? Sign up"
      AuthMode.SignUp -> "Have an account? Sign in"
    }
