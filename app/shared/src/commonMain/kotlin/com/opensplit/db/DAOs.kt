package com.opensplit.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {
  @Transaction
  @Query("SELECT * FROM households")
  fun getHouseholdsWithMembers(): Flow<List<HouseholdWithMembers>>

  @Transaction
  @Query("SELECT * FROM households WHERE id = :id")
  fun observeHouseholdWithMembers(id: String): Flow<HouseholdWithMembers?>

  @Transaction
  @Query("SELECT * FROM households WHERE id = :id")
  suspend fun getHouseholdWithMembers(id: String): HouseholdWithMembers?

  @Query("SELECT * FROM households") fun getHouseholds(): Flow<List<HouseholdEntity>>

  @Query("SELECT * FROM households WHERE id = :id")
  suspend fun getHousehold(id: String): HouseholdEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHouseholds(households: List<HouseholdEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMembers(members: List<MemberEntity>)

  @Query("DELETE FROM household_members WHERE householdId = :householdId")
  suspend fun deleteMembersByHousehold(householdId: String)

  @Transaction
  suspend fun insertHouseholdWithMembers(household: HouseholdEntity, members: List<MemberEntity>) {
    insertHouseholds(listOf(household))
    deleteMembersByHousehold(household.id)
    insertMembers(members)
  }

  @Transaction
  suspend fun deleteHousehold(id: String) {
    deleteMembersByHousehold(id)
    deleteById(id)
  }

  @Query("DELETE FROM households WHERE id = :id") suspend fun deleteById(id: String)

  @Query("DELETE FROM households") suspend fun deleteAll()
}

@Dao
interface ExpenseDao {
  @Query(
      "SELECT * FROM expenses WHERE householdId = :householdId ORDER BY createdAtEpochMillis DESC"
  )
  fun getExpenses(householdId: String): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE id = :id") suspend fun getExpense(id: String): ExpenseEntity?

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
