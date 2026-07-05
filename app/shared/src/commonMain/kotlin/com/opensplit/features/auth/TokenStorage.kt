package com.opensplit.features.auth

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * Abstraction for storing authentication tokens. Implement platform-specific persistence (e.g.
 * DataStore on Android) where possible.
 */
interface TokenStorage {
  suspend fun saveAccessToken(token: String)

  suspend fun getAccessToken(): String?

  suspend fun clearAccessToken()
}

/** No-op implementation used on platforms where persistence isn't configured. */
class NoOpTokenStorage : TokenStorage {
  override suspend fun saveAccessToken(token: String) {
    delay(1.milliseconds)
  }

  override suspend fun getAccessToken(): String? {
    delay(1.milliseconds)
    return null
  }

  override suspend fun clearAccessToken() {
    delay(1.milliseconds)
  }
}
