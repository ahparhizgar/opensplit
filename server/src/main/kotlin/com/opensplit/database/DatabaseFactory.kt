package com.opensplit.database

import com.opensplit.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

fun createHikariDataSource(config: DatabaseConfig): HikariDataSource {
  val hikariConfig =
      HikariConfig().apply {
        jdbcUrl = config.jdbcUrl
        username = config.username
        password = config.password
        driverClassName = config.driverClassName
        maximumPoolSize = 10
        validate()
      }

  return HikariDataSource(hikariConfig)
}

fun connectDatabase(dataSource: HikariDataSource): Database = Database.connect(dataSource)
