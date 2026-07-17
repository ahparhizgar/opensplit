package com.opensplit

import com.opensplit.database.DatabaseInitializer
import com.opensplit.features.auth.authModule
import com.opensplit.features.health.healthModule
import com.opensplit.features.household.householdModule
import com.opensplit.plugins.configureDependencies
import com.opensplit.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.ext.inject

fun main() {
  embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::openSplit)
      .start(wait = true)
}

fun Application.openSplit(isTest: Boolean = false) {
  configureSerialization()
  configureDependencies(isTest)

  val initializer by inject<DatabaseInitializer>()
  initializer.init()

  authModule()
  householdModule()
  healthModule()
}
