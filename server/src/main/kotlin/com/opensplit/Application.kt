package com.opensplit

import com.opensplit.database.DatabaseInitializer
import com.opensplit.features.auth.authModule
import com.opensplit.features.expense.expenseModule
import com.opensplit.features.health.healthModule
import com.opensplit.features.household.householdModule
import com.opensplit.features.sync.syncModule
import com.opensplit.plugins.configureDependencies
import com.opensplit.plugins.configureHTTP
import com.opensplit.plugins.configureSecurity
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
  configureHTTP(isTest)
  configureDependencies(isTest)
  configureSerialization()
  configureSecurity()

  val initializer by inject<DatabaseInitializer>()
  initializer.init()

  authModule()
  householdModule()
  expenseModule()
  syncModule()
  healthModule()
}
