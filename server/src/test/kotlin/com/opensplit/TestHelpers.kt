package com.opensplit

import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

/**
 * Convenience wrapper around `testApplication` that installs the application module
 * (`openSplit()`). Tests inside the block can use the provided `client` property
 * and should serialize request bodies manually (e.g. using kotlinx.serialization's
 * Json.encodeToString) when necessary.
 *
 * Usage:
 * fun myTest() = testOpenSplit {
 *     val resp = client.post("/auth/sign-up") {
 *         contentType(ContentType.Application.Json)
 *         setBody(json.encodeToString(SignUpRequest.serializer(), SignUpRequest(...)))
 *     }
 * }
 */
fun testOpenSplit(block: suspend ApplicationTestBuilder.() -> Unit) =
    testApplication {
        application { openSplit() }
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
