package com.opensplit.db

import org.jetbrains.exposed.v1.core.Table

object Users : Table("users") {
  val id = varchar("id", 36)
  val name = varchar("name", 255).nullable()
  val email = varchar("email", 255).uniqueIndex()
  val passwordHash = varchar("password_hash", 64)
}
