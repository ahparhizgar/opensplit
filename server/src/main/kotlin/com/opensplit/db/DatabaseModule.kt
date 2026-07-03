package com.opensplit.db

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

fun databaseModule() = module {
  single<DatabaseConfig> { DatabaseConfig.fromEnvironment() }

  single<HikariDataSource> { createHikariDataSource(get()) }

  single<DataSource> { get<HikariDataSource>() }

  single<Database> { Database.connect(get<DataSource>()) }

  single { DatabaseInitializer(get()) }
}
