package com.opensplit.db

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.dsl.module

@OptIn(ExperimentalUuidApi::class)
fun databaseTestModule() = module {
  single {
    DatabaseConfig(
        jdbcUrl = "jdbc:h2:mem:test-${Uuid.random()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        username = "sa",
        password = "",
        driverClassName = "org.h2.Driver",
    )
  }

  single<HikariDataSource> { createHikariDataSource(get()) }

  single<DataSource> { get<HikariDataSource>() }

  single<Database> { Database.connect(get<DataSource>()) }
}
