package com.opensplit.db

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import com.opensplit.dto.expense.SyncStatus
import kotlin.time.Instant

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val inviteLink: String,
    val isOwner: Boolean,
    val lastInteractionAtEpochMillis: Instant = Instant.DISTANT_PAST,
)

@Entity(tableName = "group_members", primaryKeys = ["groupId", "userId"])
data class MemberEntity(
    val groupId: String,
    val userId: String,
    val name: String,
    val email: String,
    val isOwner: Boolean,
    val isCurrentUser: Boolean,
    val balance: Double,
)

data class GroupWithMembers(
    @Embedded val group: GroupEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["groupId"]) val members: List<MemberEntity>,
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val title: String,
    val amount: Double,
    val creator: String,
    val createdAtEpochMillis: Instant,
    val splitMethodJson: String,
    val syncStatus: SyncStatus,
)

@Entity(tableName = "participants", primaryKeys = ["expenseId", "userId"])
data class ParticipantEntity(
    val expenseId: String,
    val userId: String,
    val paidShare: Double,
    val consumedShare: Double,
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operation: OperationType,
    val entityType: String,
    val entityId: String,
    val metadata: String? = null,
    val createdAt: Instant,
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(@PrimaryKey val key: String, val value: String)

enum class OperationType {
  CREATE,
  UPDATE,
  DELETE,
}
