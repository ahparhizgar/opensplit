package com.opensplit.database

import com.opensplit.integration.sync.SyncEntityType
import com.opensplit.integration.sync.SyncOperation
import kotlin.time.Instant
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.datetime.InstantColumnType

class KotlinInstantColumnType : InstantColumnType<Instant>() {
  override fun toInstant(value: Instant): Instant = value

  override fun fromInstant(instant: Instant): Instant = instant
}

fun Table.timestamp(name: String): Column<Instant> = registerColumn(name, KotlinInstantColumnType())

object ChangeLog : Table("change_log") {
  val id = long("id").autoIncrement()
  val entityType = enumerationByName("entity_type", 50, SyncEntityType::class)
  val entityId = varchar("entity_id", 36)
  val operation = enumerationByName("operation", 20, SyncOperation::class)
  val timestamp = timestamp("timestamp")

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

object Groups : Table("groups") {
  val id = varchar("id", 36)
  val name = varchar("name", 255)
  val ownerId = varchar("owner_id", 36).references(Users.id)
  val inviteCode = varchar("invite_code", 64).nullable()
  val version = long("version").default(0)
  val lastInteractionAt = timestamp("last_interaction_at").default(Instant.DISTANT_PAST)

  override val primaryKey = PrimaryKey(id)

  init {
    uniqueIndex("uq_groups_invite_code", inviteCode)
  }
}

object Memberships : Table("memberships") {
  val id = varchar("id", 36)
  val groupId = varchar("group_id", 36).references(Groups.id)
  val userId = varchar("user_id", 36).references(Users.id)
  val balance = double("balance").default(0.0)
  val version = long("version").default(0)

  override val primaryKey = PrimaryKey(id)
}

object Expenses : Table("expenses") {
  val id = varchar("id", 36)
  val groupId = varchar("group_id", 36).references(Groups.id)
  val title = varchar("title", 255)
  val amount = double("amount")
  val creator = varchar("creator", 36).references(Users.id)
  val createdAt = timestamp("created_at")
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
