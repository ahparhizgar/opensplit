package com.opensplit.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import com.opensplit.DataDir
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

@Database(
    entities =
        [
            GroupEntity::class,
            MemberEntity::class,
            ExpenseEntity::class,
            ParticipantEntity::class,
            SyncQueueEntity::class,
            SyncMetadataEntity::class,
        ],
    version = 4,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun groupDao(): GroupDao

  abstract fun expenseDao(): ExpenseDao

  abstract fun syncQueueDao(): SyncQueueDao

  abstract fun syncMetadataDao(): SyncMetadataDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  override fun initialize(): AppDatabase
}

val MIGRATION_3_4 =
    object : Migration(3, 4) {
      override suspend fun migrate(connection: SQLiteConnection) {
        connection
            .prepare(
                "ALTER TABLE `groups` ADD COLUMN `lastInteractionAtEpochMillis` INTEGER NOT NULL DEFAULT 0",
            )
            .use { it.step() }
      }
    }

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    queryContext: CoroutineContext = Dispatchers.Default,
): AppDatabase {
  return builder
      .addMigrations(MIGRATION_3_4)
      .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
      .setQueryCoroutineContext(queryContext)
      .build()
}

interface AppDatabaseBuilderFactory {
  fun createBuilder(dataDir: DataDir): RoomDatabase.Builder<AppDatabase>
}
