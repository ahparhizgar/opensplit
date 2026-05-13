package com.opensplit

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.SessionStorageMemory
import kotlinx.serialization.json.Json
import com.opensplit.routes.healthRoute
import com.opensplit.features.auth.AuthSession
import com.opensplit.features.auth.authRoutes

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
    }
    install(Sessions) {
        cookie<AuthSession>("opensplit-auth-session", storage = SessionStorageMemory()) {
            cookie.httpOnly = true
            cookie.path = "/"
        }
    }

    // Initialize database (reads JDBC_DATABASE_URL / DATABASE_URL or falls back to in-memory H2)
    com.opensplit.db.DatabaseFactory.init()

    authRoutes()
    healthRoute()
}
