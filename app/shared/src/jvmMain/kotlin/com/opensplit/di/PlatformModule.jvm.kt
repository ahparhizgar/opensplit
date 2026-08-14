package com.opensplit.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.opensplit.DataDir
import com.opensplit.db.AppDatabase
import com.opensplit.db.AppDatabaseBuilderFactory
import java.io.File
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

class JvmAppDatabaseBuilderFactory : AppDatabaseBuilderFactory {
  override fun createBuilder(dataDir: DataDir): RoomDatabase.Builder<AppDatabase> {
    val builder =
        if (dataDir == DataDir.MEMORY) {
          Room.inMemoryDatabaseBuilder<AppDatabase>()
        } else {
          Room.databaseBuilder<AppDatabase>(name = File(dataDir.dir, "opensplit.db").absolutePath)
        }
    return builder.setDriver(BundledSQLiteDriver())
  }
}

actual fun platformModule(): Module = module {
  single { JvmAppDatabaseBuilderFactory() }.bind<AppDatabaseBuilderFactory>()
}
