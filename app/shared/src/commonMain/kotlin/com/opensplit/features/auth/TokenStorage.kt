package com.opensplit.features.auth

/**
 * Abstraction for storing authentication tokens.
 * Implement platform-specific persistence (e.g. DataStore on Android) where possible.
 */
interface TokenStorage {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun clearAccessToken()
}

/**
 * No-op implementation used on platforms where persistence isn't configured.
 */
class NoOpTokenStorage : TokenStorage {
    override suspend fun saveAccessToken(token: String) {}
    override suspend fun getAccessToken(): String? = null
    override suspend fun clearAccessToken() {}
}

