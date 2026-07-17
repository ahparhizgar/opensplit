package com.opensplit

import com.opensplit.db.DatabaseInitializer
import com.opensplit.db.databaseModule
import com.opensplit.db.databaseTestModule
import com.opensplit.features.auth.BcryptPasswordHasher
import com.opensplit.features.auth.PasswordHasher
import com.opensplit.features.auth.authModule
import com.opensplit.features.auth.authRoutes
import com.opensplit.features.health.healthRoute
import com.opensplit.features.household.householdRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() {
  embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::openSplit)
      .start(wait = true)
}

fun Application.openSplit(isTest: Boolean = false) {
  install(ContentNegotiation) {
    json(
        Json {
          ignoreUnknownKeys = true
          explicitNulls = false
        },
    )
  }
  install(Koin) {
    modules(
        databaseModule(),
        authModule(),
    )
    if (isTest) {
      modules(
          databaseTestModule(),
          module { single { BcryptPasswordHasher(cost = 4) as PasswordHasher } },
      )
    }
  }

  val initializer: DatabaseInitializer by inject()
  initializer.init()

  authRoutes()
  householdRoutes()
  healthRoute()
}
