package com.opensplit.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

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
