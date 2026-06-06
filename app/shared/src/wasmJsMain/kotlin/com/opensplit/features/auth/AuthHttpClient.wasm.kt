package com.opensplit.features.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createAuthHttpClient(): HttpClient = HttpClient(Js) {
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

actual fun getApiBaseUrl(): String = "http://127.0.0.1:8080"

actual fun platformDecodeBase64(input: String): String {
    val binaryStr = js("atob(input)")
    return binaryStr as String
}

actual fun currentTimeSeconds(): Long = (js("Math.floor(Date.now() / 1000)") as Double).toLong()
