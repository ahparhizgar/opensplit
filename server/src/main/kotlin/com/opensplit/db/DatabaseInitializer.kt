package com.opensplit.db

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DatabaseInitializer(private val database: Database) {
  fun init() {
    transaction(database) { SchemaUtils.create(Users, Households, Memberships) }
  }
}
