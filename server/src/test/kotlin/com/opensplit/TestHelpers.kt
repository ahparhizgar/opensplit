package com.opensplit

import com.opensplit.features.auth.AuthService
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

fun testOpenSplit(block: suspend ApplicationTestBuilder.() -> Unit) =
    testApplication {
        application { openSplit() }
        val auth = AuthService().signUp("registered@example.com", "password")
        client = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(DefaultRequest) {
                contentType(ContentType.Application.Json)
            }
        }
        block()
    }
