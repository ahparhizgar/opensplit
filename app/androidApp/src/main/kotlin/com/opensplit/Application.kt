package com.opensplit

import android.app.Application
import com.opensplit.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Application : Application() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidContext(this@Application)
      modules(appModule())
      modules(module { single { DataDir(filesDir.path) } })
    }
  }
}
