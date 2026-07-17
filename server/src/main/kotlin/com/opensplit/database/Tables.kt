package com.opensplit.database

import org.jetbrains.exposed.v1.core.Table

object Users : Table("users") {
  val id = varchar("id", 36)
  val name = varchar("name", 255).nullable()
  val email = varchar("email", 255).uniqueIndex()
  val passwordHash = varchar("password_hash", 64)
}

object Households : Table("households") {
  val id = varchar("id", 36)
  val name = varchar("name", 255)
  val ownerId = varchar("owner_id", 36)
  val inviteCode = varchar("invite_code", 64).nullable()

  init {
    uniqueIndex("uq_households_invite_code", inviteCode)
  }
}

object Memberships : Table("memberships") {
  val id = varchar("id", 36)
  val householdId = varchar("household_id", 36)
  val userId = varchar("user_id", 36)
}
