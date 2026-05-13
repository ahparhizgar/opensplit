package com.opensplit.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.StdOutSqlLogger

object DatabaseFactory {
    fun init() {
        val dbUrl = System.getenv("JDBC_DATABASE_URL") ?: System.getenv("DATABASE_URL")
        val user = System.getenv("DB_USER") ?: System.getenv("POSTGRES_USER") ?: "sa"
        val pass = System.getenv("DB_PASSWORD") ?: System.getenv("POSTGRES_PASSWORD") ?: ""
        val jdbcUrl = dbUrl ?: "jdbc:h2:mem:opensplit;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"

        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            username = user
            password = pass
            driverClassName = if (jdbcUrl.startsWith("jdbc:h2")) "org.h2.Driver" else "org.postgresql.Driver"
            maximumPoolSize = 3
        }
        val ds = HikariDataSource(config)
        Database.connect(ds)
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)
        }
    }
}