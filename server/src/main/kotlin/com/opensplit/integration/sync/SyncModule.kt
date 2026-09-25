package com.opensplit.integration.sync

import org.koin.dsl.module

fun syncKoinModule() = module {
  single<SyncRepository> { SyncRepositoryImpl(get()) }
  single { SyncService(get()) }
}
