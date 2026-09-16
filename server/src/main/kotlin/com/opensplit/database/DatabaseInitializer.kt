package com.opensplit.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

class DatabaseInitializer(private val database: Database) {
  fun init() {
    transaction(database) {
      val statements =
          MigrationUtils.statementsRequiredForDatabaseMigration(
              ChangeLog,
              Users,
              Households,
              Memberships,
              Expenses,
              ExpenseParticipants,
              withLogs = true,
          )

      for (stmt in statements) {
        if (stmt.trimStart().startsWith("DROP ", ignoreCase = true)) {
          continue
        }
        exec(stmt)
      }
    }
  }
}
