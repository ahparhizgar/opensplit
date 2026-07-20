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

@Serializable
data class ErrorResponse(
    val generalError: String,
    val errors: Map<String, String> = emptyMap(),
)
