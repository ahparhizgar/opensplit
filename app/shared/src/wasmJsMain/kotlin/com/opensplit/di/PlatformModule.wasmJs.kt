package com.opensplit.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.opensplit.DataDir
import com.opensplit.db.AppDatabase
import com.opensplit.db.AppDatabaseBuilderFactory
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

class WasmAppDatabaseBuilderFactory : AppDatabaseBuilderFactory {
  override fun createBuilder(dataDir: DataDir): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(name = "opensplit.db")
  }
}

actual fun platformModule(): Module = module {
  single { WasmAppDatabaseBuilderFactory() }.bind<AppDatabaseBuilderFactory>()
}
