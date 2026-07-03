package com.opensplit.validation.auth

data class ValidationResult(
    val errors: Map<String, String> = emptyMap(),
) {
  val isValid: Boolean
    get() = errors.isEmpty()
}

object AuthValidation {
  private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

  fun validateSignUp(email: String, password: String): ValidationResult =
      validateCredentials(email, password)

  fun validateSignIn(email: String, password: String): ValidationResult =
      validateCredentials(email, password)

  private fun validateCredentials(email: String, password: String): ValidationResult {
    val errors = linkedMapOf<String, String>()
    validateEmail(email)?.let { errors["email"] = it }
    validatePassword(password)?.let { errors["password"] = it }
    return ValidationResult(errors)
  }

  fun validateEmail(email: String): String? =
      when {
        email.isBlank() -> "Email is required"
        !emailPattern.matches(email) -> "Enter a valid email address"
        else -> null
      }

  fun validatePassword(password: String): String? =
      when {
        password.isBlank() -> "Password is required"
        password.length < 8 -> "Password must be at least 8 characters"
        else -> null
      }
}
