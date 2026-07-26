package com.opensplit.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.opensplit.DataDir
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

@Database(
    entities =
        [
            HouseholdEntity::class,
            ExpenseEntity::class,
            ParticipantEntity::class,
            SyncQueueEntity::class,
        ],
    version = 1,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun householdDao(): HouseholdDao

  abstract fun expenseDao(): ExpenseDao

  abstract fun syncQueueDao(): SyncQueueDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    queryContext: CoroutineContext = Dispatchers.Default,
): AppDatabase {
  return builder.setQueryCoroutineContext(queryContext).build()
}

interface AppDatabaseBuilderFactory {
  fun createBuilder(dataDir: DataDir): RoomDatabase.Builder<AppDatabase>
}
