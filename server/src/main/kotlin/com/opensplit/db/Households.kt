package com.opensplit.db

import org.jetbrains.exposed.sql.Table

object Households : Table("households") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val ownerId = varchar("owner_id", 36)
    val inviteCode = varchar("invite_code", 64).nullable()
}

object Memberships : Table("memberships") {
    val id = varchar("id", 36)
    val householdId = varchar("household_id", 36)
    val userId = varchar("user_id", 36)
}
