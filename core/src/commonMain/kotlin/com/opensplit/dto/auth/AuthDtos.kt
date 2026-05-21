package com.opensplit.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthSessionState(
    val userId: String,
    val email: String,
    val householdId: String? = null,
    val accessToken: String,
)

@Serializable
data class ErrorResponse(
    val generalError: String? = null,
    val errors: Map<String, String> = emptyMap(),
)

@Serializable
data class HouseholdContextState(
    val authenticated: Boolean,
    val email: String,
    val householdId: String? = null,
    val message: String,
)
