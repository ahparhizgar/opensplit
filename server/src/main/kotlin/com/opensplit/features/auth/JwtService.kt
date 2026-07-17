package com.opensplit.features.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.opensplit.config.JwtConfig
import java.util.Date

class JwtService(private val jwtConfig: JwtConfig) {
  fun issue(userId: String, email: String): String {
    val now = Date()
    val expiry = Date(now.time + jwtConfig.expiryMs)
    val algorithm = Algorithm.HMAC256(jwtConfig.secret)
    return JWT.create()
        .withSubject(userId)
        .withClaim("email", email)
        .withIssuedAt(now)
        .withExpiresAt(expiry)
        .sign(algorithm)
  }

  fun verify(token: String): String? {
    return try {
      val algorithm = Algorithm.HMAC256(jwtConfig.secret)
      val verifier = JWT.require(algorithm).build()
      verifier.verify(token).subject
    } catch (_: Exception) {
      null
    }
  }
}
