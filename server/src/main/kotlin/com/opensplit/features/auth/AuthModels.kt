package com.opensplit.features.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.opensplit.db.Users
import java.util.UUID
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

interface PasswordHasher {
  fun hash(password: String): String

  fun verify(password: String, hash: String): Boolean
}

class BcryptPasswordHasher : PasswordHasher {
  override fun hash(password: String): String =
      BCrypt.withDefaults().hashToString(12, password.toCharArray())

  override fun verify(password: String, hash: String): Boolean =
      BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}

@Serializable
data class AuthSession(
    val userId: String,
    val name: String? = null,
    val email: String,
    val householdId: String? = null,
    val accessToken: String,
)

data class RegisteredUser(
    val userId: String,
    val name: String? = null,
    val email: String,
    val passwordHash: String,
)

class AuthService(
    private val passwordHasher: PasswordHasher = BcryptPasswordHasher(),
) {
  fun signUp(email: String, password: String, name: String? = null): AuthSession {
    val existing = transaction { Users.select { Users.email eq email }.limit(1).firstOrNull() }
    require(existing == null) { "Email already exists" }
    val userId = UUID.randomUUID().toString()
    val passwordHash = passwordHasher.hash(password)
    transaction {
      Users.insert {
        it[Users.id] = userId
        it[Users.name] = name
        it[Users.email] = email
        it[Users.passwordHash] = passwordHash
      }
    }
    val user =
        RegisteredUser(userId = userId, name = name, email = email, passwordHash = passwordHash)
    return user.toSessionState()
  }

  fun signIn(email: String, password: String): AuthSession {
    val row =
        transaction { Users.select { Users.email eq email }.limit(1).firstOrNull() }
            ?: throw IllegalArgumentException("Invalid credentials")
    val storedHash = row[Users.passwordHash]
    if (!passwordHasher.verify(password, storedHash))
        throw IllegalArgumentException("Invalid credentials")
    val user =
        RegisteredUser(
            userId = row[Users.id],
            name = row[Users.name],
            email = row[Users.email],
            passwordHash = storedHash,
        )
    return user.toSessionState()
  }

  fun hasUser(email: String): Boolean = transaction { Users.select { Users.email eq email }.any() }

  private fun RegisteredUser.toSessionState(): AuthSession =
      AuthSession(
          userId = userId,
          name = name,
          email = email,
          householdId = null,
          accessToken = JwtTokenService.issue(userId, email),
      )
}

class JwtService(
    private val secret: String,
    private val expiryMs: Long,
) {
  fun issue(userId: String, email: String): String {
    val now = java.util.Date()
    val expiry = java.util.Date(now.time + expiryMs)
    val algorithm = com.auth0.jwt.algorithms.Algorithm.HMAC256(secret)
    return com.auth0.jwt.JWT.create()
        .withSubject(userId)
        .withClaim("email", email)
        .withIssuedAt(now)
        .withExpiresAt(expiry)
        .sign(algorithm)
  }

  fun verify(token: String): String? {
    return try {
      val algorithm = com.auth0.jwt.algorithms.Algorithm.HMAC256(secret)
      val verifier = com.auth0.jwt.JWT.require(algorithm).build()
      val decoded = verifier.verify(token)
      decoded.subject
    } catch (_: Exception) {
      null
    }
  }
}

object JwtTokenService {
  private val secret: String = System.getenv("JWT_SECRET") ?: "dev-secret-change-in-production"
  private val expiryMs: Long = (System.getenv("JWT_EXPIRY_MS")?.toLongOrNull()) ?: 86_400_000L
  private val service = JwtService(secret = secret, expiryMs = expiryMs)

  fun issue(userId: String, email: String): String = service.issue(userId, email)

  fun verify(token: String): String? = service.verify(token)
}
