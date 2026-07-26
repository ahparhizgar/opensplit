package com.opensplit.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.opensplit.dto.expense.SyncStatus

@Entity(tableName = "households")
data class HouseholdEntity(
    @PrimaryKey val id: String,
    val name: String,
    val inviteLink: String,
    val balance: Double,
    val isOwner: Boolean,
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val householdId: String,
    val title: String,
    val amount: Double,
    val payerId: String,
    val createdAtEpochMillis: Long,
    val splitMethodJson: String,
    val syncStatus: SyncStatus,
)

@Entity(tableName = "participants", primaryKeys = ["expenseId", "userId"])
data class ParticipantEntity(
    val expenseId: String,
    val userId: String,
    val paidShare: Double,
    val owedShare: Double,
    val netBalance: Double,
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operation: OperationType,
    val entityId: String,
    val payloadJson: String,
    val createdAt: Long,
)

enum class OperationType {
  CREATE_EXPENSE,
  UPDATE_EXPENSE,
  DELETE_EXPENSE,
}
