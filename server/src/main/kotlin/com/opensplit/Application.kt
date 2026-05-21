package com.opensplit

import com.opensplit.db.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.opensplit.routes.healthRoute
import com.opensplit.routes.householdRoutes
import com.opensplit.features.auth.authRoutes
import com.opensplit.features.auth.configureJwtAuth

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
    // Initialize database (reads JDBC_DATABASE_URL / DATABASE_URL or falls back to in-memory H2)
    DatabaseFactory.init()

    configureJwtAuth()
    authRoutes()
    householdRoutes()
    healthRoute()
}
