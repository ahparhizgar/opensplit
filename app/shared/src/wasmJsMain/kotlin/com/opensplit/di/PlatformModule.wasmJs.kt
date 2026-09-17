package com.opensplit.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.opensplit.DataDir
import com.opensplit.db.AppDatabase
import com.opensplit.db.AppDatabaseBuilderFactory
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.w3c.dom.Worker

class WasmAppDatabaseBuilderFactory : AppDatabaseBuilderFactory {
  override fun createBuilder(dataDir: DataDir): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(name = "opensplit.db")
        .setDriver(createSQLiteWasmWorker())
  }
}

actual fun platformModule(): Module = module {
  single { WasmAppDatabaseBuilderFactory() }.bind<AppDatabaseBuilderFactory>()
}

fun createSQLiteWasmWorker() = WebWorkerSQLiteDriver(jsWorker())

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsWorker(): Worker =
    js("""new Worker(new URL("sqlite-wasm-worker/worker.js", import.meta.url))""")
