package com.opensplit.features.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createAuthHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
    }
}

actual fun getApiBaseUrl(): String = System.getenv("API_BASE_URL") ?: "http://127.0.0.1:8080"

actual fun platformDecodeBase64(input: String): String =
    java.util.Base64.getDecoder().decode(input).decodeToString()

actual fun currentTimeSeconds(): Long = System.currentTimeMillis() / 1000
