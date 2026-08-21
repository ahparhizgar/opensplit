package com.opensplit.config

data class JwtConfig(
    val secret: String,
    val expiryMs: Long,
) {
  companion object {
    fun fromEnvironment(): JwtConfig {
      val secret = System.getenv("JWT_SECRET") ?: "dev-secret-change-in-production"
      val expiryMs = System.getenv("JWT_EXPIRY_MS")?.toLongOrNull() ?: 0L // 0 means infinity
      return JwtConfig(secret = secret, expiryMs = expiryMs)
    }
  }
}
