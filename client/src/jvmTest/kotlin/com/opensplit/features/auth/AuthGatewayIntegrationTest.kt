package com.opensplit.features.auth

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthGatewayIntegrationTest {
    @Test
    fun gatewayPerformsRealNetworkAuthRequest() {
        val server = HttpServer.create(InetSocketAddress(19080), 0)
        server.createContext("/auth/sign-up") { exchange ->
            val body = """{"userId":"user-1","email":"net@example.com","householdId":null,"accessToken":"token-user-1-net@example.com"}"""
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(201, body.toByteArray().size.toLong())
            exchange.responseBody.use { output -> output.write(body.toByteArray()) }
        }
        server.createContext("/household-context") { exchange ->
            val authHeader = exchange.requestHeaders.getFirst("Authorization") ?: ""
            val body = if (authHeader.startsWith("Bearer token-user-1-net@example.com")) {
                """{"authenticated":true,"email":"net@example.com","householdId":null,"message":"Authenticated household context"}"""
            } else {
                """{"errors":{"token":"Sign in required"}}"""
            }
            val status = if (authHeader.startsWith("Bearer token-user-1-net@example.com")) 200 else 401
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, body.toByteArray().size.toLong())
            exchange.responseBody.use { output -> output.write(body.toByteArray()) }
        }
        server.start()

        try {
            val gateway = KtorAuthGateway(createAuthHttpClient(), "http://127.0.0.1:19080")

            val result = runBlocking {
                gateway.signUp("net@example.com", "password123")
            }

            assertEquals("net@example.com", result.session.email)
            assertNotNull(result.householdContext)
        } finally {
            server.stop(0)
        }
    }
}
