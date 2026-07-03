package com.opensplit.features.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.Foundation.create

actual fun createAuthHttpClient(): HttpClient =
    HttpClient(Darwin) {
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
  val nsData = NSData.create(base64Encoded = input, options = 0) ?: return ""
  return nsData.toString()
}

actual fun currentTimeSeconds(): Long =
    (platform.Foundation.NSDate().timeIntervalSince1970).toLong()
