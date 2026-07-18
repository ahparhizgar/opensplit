package com.opensplit.plugins

import com.opensplit.config.DatabaseConfig
import com.opensplit.database.DatabaseInitializer
import com.opensplit.database.connectDatabase
import com.opensplit.database.createHikariDataSource
import com.opensplit.features.auth.authKoinModule
import com.opensplit.features.auth.testAuthKoinModule
import com.opensplit.features.expense.expenseKoinModule
import com.opensplit.features.household.householdKoinModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDependencies(isTest: Boolean) {
  install(Koin) {
    modules(
        module {
          single<DatabaseConfig> {
            if (isTest) {
              DatabaseConfig.test()
            } else {
              DatabaseConfig.fromEnvironment()
            }
          }
          single { createHikariDataSource(get()) }
          single { connectDatabase(get()) }
          single { DatabaseInitializer(get()) }
        },
        authKoinModule(),
        householdKoinModule(),
        expenseKoinModule(),
    )
    if (isTest) {
      modules(testAuthKoinModule())
    }
  }
}
