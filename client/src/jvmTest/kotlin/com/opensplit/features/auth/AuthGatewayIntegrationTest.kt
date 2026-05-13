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
            val body = """{"userId":"user-1","email":"net@example.com","householdId":null}"""
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(201, body.toByteArray().size.toLong())
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
