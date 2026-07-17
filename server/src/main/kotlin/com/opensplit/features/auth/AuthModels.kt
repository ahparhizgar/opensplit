package com.opensplit.features.auth

data class AuthUser(
    val id: String,
    val name: String?,
    val email: String,
    val passwordHash: String,
)

class DuplicateEmailException : RuntimeException()

class InvalidCredentialsException : RuntimeException()
