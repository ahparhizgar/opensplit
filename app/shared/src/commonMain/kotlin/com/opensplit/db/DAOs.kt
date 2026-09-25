package com.opensplit.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
  @Transaction
  @Query("SELECT * FROM groups")
  fun getGroupsWithMembers(): Flow<List<GroupWithMembers>>

  @Transaction
  @Query("SELECT * FROM groups WHERE id = :id")
  fun observeGroupWithMembers(id: String): Flow<GroupWithMembers?>

  @Transaction
  @Query("SELECT * FROM groups WHERE id = :id")
  suspend fun getGroupWithMembers(id: String): GroupWithMembers?

  @Query("SELECT * FROM groups") fun getGroups(): Flow<List<GroupEntity>>

  @Query("SELECT * FROM groups WHERE id = :id") suspend fun getGroup(id: String): GroupEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGroups(groups: List<GroupEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMembers(members: List<MemberEntity>)

  @Query("DELETE FROM group_members WHERE groupId = :groupId")
  suspend fun deleteMembersByGroup(groupId: String)

  @Transaction
  suspend fun insertGroupWithMembers(group: GroupEntity, members: List<MemberEntity>) {
    insertGroups(listOf(group))
    deleteMembersByGroup(group.id)
    insertMembers(members)
  }

  @Query(
      "UPDATE group_members SET balance = balance + :delta WHERE groupId = :groupId AND userId = :userId"
  )
  suspend fun updateMemberBalance(groupId: String, userId: String, delta: Double)

  @Query("UPDATE groups SET lastInteractionAtEpochMillis = :timestamp WHERE id = :groupId")
  suspend fun updateLastInteraction(groupId: String, timestamp: Instant)

  @Transaction
  suspend fun deleteGroup(id: String) {
    deleteMembersByGroup(id)
    deleteById(id)
  }

  @Query("DELETE FROM groups WHERE id = :id") suspend fun deleteById(id: String)

  @Query("DELETE FROM groups") suspend fun deleteAll()
}

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAtEpochMillis DESC")
  fun getExpenses(groupId: String): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE id = :id") suspend fun getExpense(id: String): ExpenseEntity?

  @Query("SELECT * FROM expenses WHERE id = :id")
  fun observeExpense(id: String): Flow<ExpenseEntity?>

  @Query("SELECT * FROM participants WHERE expenseId = :expenseId")
  suspend fun getParticipants(expenseId: String): List<ParticipantEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertExpense(expense: ExpenseEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertParticipants(participants: List<ParticipantEntity>)

  @Query("DELETE FROM expenses WHERE id = :id") suspend fun deleteExpense(id: String)

  @Query("DELETE FROM participants WHERE expenseId = :expenseId")
  suspend fun deleteParticipants(expenseId: String)

  @Transaction
  suspend fun insertExpenseWithParticipants(
      expense: ExpenseEntity,
      participants: List<ParticipantEntity>,
  ) {
    insertExpense(expense)
    deleteParticipants(expense.id)
    insertParticipants(participants)
  }
}

@Dao
interface SyncQueueDao {
  @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
  fun getQueue(): Flow<List<SyncQueueEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun enqueue(entry: SyncQueueEntity)

  @Delete suspend fun dequeue(entry: SyncQueueEntity)

  @Query("DELETE FROM sync_queue WHERE entityId = :entityId")
  suspend fun removeByEntityId(entityId: String)
}

@Dao
interface SyncMetadataDao {
  @Query("SELECT value FROM sync_metadata WHERE `key` = :key")
  suspend fun getMetadata(key: String): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMetadata(metadata: SyncMetadataEntity)
}
