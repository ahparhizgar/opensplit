package com.opensplit

import com.opensplit.features.auth.AuthService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.ktor.ext.inject

fun testOpenSplit(block: suspend ApplicationTestBuilder.() -> Unit) =
    testApplication {
        var token = ""
        application {
            openSplit(isTest = true)
            val authService by inject<AuthService>()
            val auth = authService.signUp("registersdf@example.com", "password")
            token = auth.accessToken
        }
        startApplication()
        client = createTestClient(token)
        block()
    }

fun ApplicationTestBuilder.createAuthenticatedClient(token: String): HttpClient = createClient {
    install(ContentNegotiation) {
        json()
    }

    install(DefaultRequest) {
        contentType(ContentType.Application.Json)
    }

    install(Auth) {
        bearer {
            cacheTokens = false
            loadTokens {
                BearerTokens(accessToken = token, refreshToken = null)
            }
        }
    }
}

private fun ApplicationTestBuilder.createTestClient(token: String): HttpClient = createAuthenticatedClient(token)
