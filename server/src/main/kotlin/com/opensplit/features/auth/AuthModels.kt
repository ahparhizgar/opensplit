package com.opensplit.features.auth

import com.opensplit.dto.auth.AuthSessionState
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.util.UUID

@Serializable
data class AuthSession(
    val userId: String,
    val email: String,
    val householdId: String? = null,
)

data class RegisteredUser(
    val userId: String,
    val email: String,
    val passwordHash: String,
)

class AuthService {
    private val usersByEmail = linkedMapOf<String, RegisteredUser>()

    fun signUp(email: String, password: String): AuthSessionState {
        require(!usersByEmail.containsKey(email)) { "Email already exists" }
        val user = RegisteredUser(
            userId = UUID.randomUUID().toString(),
            email = email,
            passwordHash = hashPassword(password),
        )
        usersByEmail[email] = user
        return user.toSessionState()
    }

    fun signIn(email: String, password: String): AuthSessionState {
        val user = usersByEmail[email] ?: throw IllegalArgumentException("Invalid credentials")
        if (user.passwordHash != hashPassword(password)) {
            throw IllegalArgumentException("Invalid credentials")
        }
        return user.toSessionState()
    }

    fun hasUser(email: String): Boolean = usersByEmail.containsKey(email)

    private fun RegisteredUser.toSessionState(): AuthSessionState = AuthSessionState(
        userId = userId,
        email = email,
        householdId = null,
    )

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}

fun AuthSession.toSessionState(): AuthSessionState = AuthSessionState(
    userId = userId,
    email = email,
    householdId = householdId,
)
