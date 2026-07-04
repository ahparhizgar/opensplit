package com.opensplit.ktor

import com.ahparhizgar.katch.ktor.ClientErrorExtras
import com.ahparhizgar.katch.ktor.KatchPlugin
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.features.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(tokenStorage: TokenStorage): HttpClient = HttpClient {
  defaultRequest {
    // Placeholder URL, replace with actual base URL
    url("https://localhost:8080")
    contentType(ContentType.Application.Json)
  }

  install(ContentNegotiation) {
    json(
        Json {
          ignoreUnknownKeys = true
          explicitNulls = false
        },
    )
  }

  install(Logging) {
    this.level = LogLevel.ALL
    this.logger = logger
  }

  install(Auth) {
    bearer {
      cacheTokens = false
      loadTokens { BearerTokens(tokenStorage.getAccessToken() ?: "", "") }
    }
  }

  install(KatchPlugin) {
    extractPayload {
      val body = it.body<ErrorResponse>()
      ClientErrorExtras(
          userMessage = body.generalError,
          payload = body.errors,
      )
    }
  }
}
