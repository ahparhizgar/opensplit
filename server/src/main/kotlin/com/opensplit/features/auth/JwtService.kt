package com.opensplit.features.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.opensplit.config.JwtConfig
import java.util.Date

class JwtService(private val jwtConfig: JwtConfig) {
  val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(jwtConfig.secret)).build()

  fun issue(userId: String, email: String, name: String? = null): String {
    val now = Date()
    val expiry = Date(now.time + jwtConfig.expiryMs)
    val algorithm = Algorithm.HMAC256(jwtConfig.secret)
    return JWT.create()
        .withSubject(userId)
        .withClaim("email", email)
        .apply { name?.let { withClaim("name", it) } }
        .withIssuedAt(now)
        .withExpiresAt(expiry)
        .sign(algorithm)
  }

  fun verify(token: String): String? {
    return try {
      verifier.verify(token).subject
    } catch (_: Exception) {
      null
    }
  }
}
