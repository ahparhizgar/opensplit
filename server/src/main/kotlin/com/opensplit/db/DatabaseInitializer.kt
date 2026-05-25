package com.opensplit.db


import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseInitializer(
    private val database: Database
) {
    fun init() {
        transaction(database) {
            SchemaUtils.create(
                Users,
                Households,
                Memberships
            )
        }
    }
}
