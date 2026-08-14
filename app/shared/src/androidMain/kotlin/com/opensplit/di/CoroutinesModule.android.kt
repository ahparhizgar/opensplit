package com.opensplit.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual fun coroutinesModule() = module {
  factory<CoroutineDispatcher>(named("default")) { Dispatchers.Default }
  factory<CoroutineDispatcher>(named("io")) { Dispatchers.IO }
  factory<CoroutineDispatcher>(named("main")) { Dispatchers.Main }
  factory<CoroutineDispatcher>(named("unconfined")) { Dispatchers.Unconfined }
}
