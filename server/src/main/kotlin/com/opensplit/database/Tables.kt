package com.opensplit.database

import org.jetbrains.exposed.v1.core.Table

object ChangeLog : Table("change_log") {
  val id = long("id").autoIncrement()
  val entityType = varchar("entity_type", 50)
  val entityId = varchar("entity_id", 36)
  val operation = varchar("operation", 20) // INSERT, UPDATE, DELETE
  val timestamp = long("timestamp")

  override val primaryKey = PrimaryKey(id)
}

object Users : Table("users") {
  val id = varchar("id", 36)
  val name = varchar("name", 255)
  val email = varchar("email", 255).uniqueIndex()
  val passwordHash = varchar("password_hash", 64)
  val version = long("version").default(0)

  override val primaryKey = PrimaryKey(id)
}

object Households : Table("households") {
  val id = varchar("id", 36)
  val name = varchar("name", 255)
  val ownerId = varchar("owner_id", 36).references(Users.id)
  val inviteCode = varchar("invite_code", 64).nullable()
  val version = long("version").default(0)

  override val primaryKey = PrimaryKey(id)

  init {
    uniqueIndex("uq_households_invite_code", inviteCode)
  }
}

object Memberships : Table("memberships") {
  val id = varchar("id", 36)
  val householdId = varchar("household_id", 36).references(Households.id)
  val userId = varchar("user_id", 36).references(Users.id)
  val balance = double("balance").default(0.0)
  val version = long("version").default(0)

  override val primaryKey = PrimaryKey(id)
}

object Expenses : Table("expenses") {
  val id = varchar("id", 36)
  val householdId = varchar("household_id", 36).references(Households.id)
  val title = varchar("title", 255)
  val amount = double("amount")
  val creator = varchar("creator", 36).references(Users.id)
  val createdAt = long("created_at")
  val splitMethod = text("split_method")
  val version = long("version").default(0)

  override val primaryKey = PrimaryKey(id)
}

object ExpenseParticipants : Table("expense_participants") {
  val id = varchar("id", 36)
  val expenseId = varchar("expense_id", 36).references(Expenses.id)
  val userId = varchar("user_id", 36).references(Users.id)
  val paidAmount = double("paid_amount")
  val owedAmount = double("owed_amount")
  val version = long("version").default(0)

  override val primaryKey = PrimaryKey(id)
}
