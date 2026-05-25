package com.opensplit.db

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String,
) {
    companion object {
        fun fromEnvironment(): DatabaseConfig {

            val dbUrl =
                System.getenv("JDBC_DATABASE_URL")
                    ?: System.getenv("DATABASE_URL")

            val jdbcUrl =
                dbUrl ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"

            return DatabaseConfig(
                jdbcUrl = jdbcUrl,
                username =
                    System.getenv("DB_USER")
                        ?: System.getenv("POSTGRES_USER")
                        ?: "sa",

                password =
                    System.getenv("DB_PASSWORD")
                        ?: System.getenv("POSTGRES_PASSWORD")
                        ?: "",

                driverClassName =
                    if (jdbcUrl.startsWith("jdbc:h2"))
                        "org.h2.Driver"
                    else
                        "org.postgresql.Driver"
            )
        }
    }
}