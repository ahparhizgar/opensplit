package com.opensplit.features.auth

import com.opensplit.db.Users
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class AuthSession(
    val userId: String,
    val email: String,
    val householdId: String? = null,
    val accessToken: String,
)

data class RegisteredUser(
    val userId: String,
    val email: String,
    val passwordHash: String,
)

class AuthService {

    fun signUp(email: String, password: String): AuthSession {
        val existing = transaction { Users.select { Users.email eq email }.limit(1).firstOrNull() }
        require(existing == null) { "Email already exists" }
        val userId = UUID.randomUUID().toString()
        val passwordHash = hashPassword(password)
        transaction {
            Users.insert {
                it[Users.id] = userId
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
            }
        }
        val user = RegisteredUser(userId = userId, email = email, passwordHash = passwordHash)
        return user.toSessionState()
    }

    fun signIn(email: String, password: String): AuthSession {
        val row = transaction {
            Users.select { Users.email eq email }.limit(1).firstOrNull()
        } ?: throw IllegalArgumentException("Invalid credentials")
        val storedHash = row[Users.passwordHash]
        if (storedHash != hashPassword(password)) throw IllegalArgumentException("Invalid credentials")
        val user = RegisteredUser(
            userId = row[Users.id],
            email = row[Users.email],
            passwordHash = storedHash,
        )
        return user.toSessionState()
    }

    fun hasUser(email: String): Boolean = transaction { Users.select { Users.email eq email }.any() }

    private fun RegisteredUser.toSessionState(): AuthSession = AuthSession(
        userId = userId,
        email = email,
        householdId = null,
        accessToken = JwtTokenService.issue(userId, email),
    )

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}

object JwtTokenService {
    fun issue(userId: String, email: String): String = "jwt-$userId-$email"
}
