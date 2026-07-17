package com.opensplit.plugins

import com.opensplit.config.DatabaseConfig
import com.opensplit.config.JwtConfig
import com.opensplit.database.DatabaseInitializer
import com.opensplit.database.connectDatabase
import com.opensplit.database.createHikariDataSource
import com.opensplit.features.auth.AuthRepository
import com.opensplit.features.auth.AuthRepositoryImpl
import com.opensplit.features.auth.AuthService
import com.opensplit.features.auth.BcryptPasswordHasher
import com.opensplit.features.auth.JwtService
import com.opensplit.features.auth.PasswordHasher
import com.opensplit.features.household.HouseholdRepository
import com.opensplit.features.household.HouseholdRepositoryImpl
import com.opensplit.features.household.HouseholdService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide

fun Application.configureDependencies(isTest: Boolean) {
  dependencies {
    provide<DatabaseConfig> {
      if (isTest) {
        DatabaseConfig.test()
      } else {
        DatabaseConfig.fromEnvironment()
      }
    }
    provide(::createHikariDataSource)
    provide(::connectDatabase)
    provide(::DatabaseInitializer)
    provide<PasswordHasher> {
      if (isTest) {
        BcryptPasswordHasher(4)
      } else {
        BcryptPasswordHasher(12)
      }
    }
    provide<JwtConfig> { JwtConfig.fromEnvironment() }
    provide(::JwtService)
    provide<AuthRepository>(::AuthRepositoryImpl)
    provide(::AuthService)
    provide<HouseholdRepository>(::HouseholdRepositoryImpl)
    provide(::HouseholdService)
  }
}
