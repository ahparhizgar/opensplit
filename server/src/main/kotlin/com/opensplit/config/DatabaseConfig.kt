package com.opensplit.config

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String,
) {
  companion object {
    fun fromEnvironment(): DatabaseConfig {
      val databaseUrl = System.getenv("JDBC_DATABASE_URL") ?: System.getenv("DATABASE_URL")
      val jdbcUrl = databaseUrl ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
      val username = System.getenv("DB_USER") ?: System.getenv("POSTGRES_USER") ?: "sa"
      val password = System.getenv("DB_PASSWORD") ?: System.getenv("POSTGRES_PASSWORD") ?: ""
      val driverClassName =
          if (jdbcUrl.startsWith("jdbc:h2")) {
            "org.h2.Driver"
          } else {
            "org.postgresql.Driver"
          }
      return DatabaseConfig(
          jdbcUrl = jdbcUrl,
          username = username,
          password = password,
          driverClassName = driverClassName,
      )
    }

    fun test(): DatabaseConfig =
        DatabaseConfig(
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            username = "sa",
            password = "",
            driverClassName = "org.h2.Driver",
        )
  }
}
