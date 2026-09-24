package com.opensplit.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String,
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResult(
    val userId: String,
    val name: String? = null,
    val email: String,
    val accessToken: String,
)

object FakeAuthResultFactory {
  fun create(
      userId: String = "user-1",
      name: String? = "Amir Hossein Parhizgar",
      email: String = "amir@example.com",
      accessToken: String = "fake-jwt-token",
  ) =
      AuthResult(
          userId = userId,
          name = name,
          email = email,
          accessToken = accessToken,
      )
}

@Serializable
data class UserProfile(
    val id: String,
    val name: String?,
    val email: String,
)

object FakeUserProfileFactory {
  fun create(
      id: String = "user-1",
      name: String? = "Amir Hossein Parhizgar",
      email: String = "amir@example.com",
  ) =
      UserProfile(
          id = id,
          name = name,
          email = email,
      )
}

@Serializable
data class ErrorResponse(
    val generalError: String,
    val errors: Map<String, String> = emptyMap(),
)
