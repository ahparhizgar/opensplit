package com.opensplit.features.auth

data class AuthUser(
    val id: String,
    val name: String?,
    val email: String,
    val passwordHash: String,
)

data class UserPrincipal(
    val userId: String,
    val email: String,
    val name: String?,
)

class DuplicateEmailException : RuntimeException()

class InvalidCredentialsException : RuntimeException()
