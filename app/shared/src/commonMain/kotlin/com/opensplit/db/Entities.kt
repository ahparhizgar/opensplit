package com.opensplit.db

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import com.opensplit.dto.expense.SyncStatus

@Entity(tableName = "households")
data class HouseholdEntity(
    @PrimaryKey val id: String,
    val name: String,
    val inviteLink: String,
    val balance: Double,
    val isOwner: Boolean,
)

@Entity(tableName = "household_members", primaryKeys = ["householdId", "userId"])
data class MemberEntity(
    val householdId: String,
    val userId: String,
    val name: String,
    val email: String,
    val isOwner: Boolean,
    val isCurrentUser: Boolean,
    val balance: Double,
    val balanceCurrency: String,
)

data class HouseholdWithMembers(
    @Embedded val household: HouseholdEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["householdId"])
    val members: List<MemberEntity>,
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
    val entityType: String,
    val entityId: String,
    val metadata: String? = null,
    val createdAt: Long,
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(@PrimaryKey val key: String, val value: String)

enum class OperationType {
  CREATE,
  UPDATE,
  DELETE,
}
