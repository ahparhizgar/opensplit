package com.opensplit.di

import com.opensplit.datastore.DataStoreTokenStorage
import com.opensplit.datastore.createDataStore
import com.opensplit.db.AppDatabase
import com.opensplit.db.AppDatabaseBuilderFactory
import com.opensplit.db.getRoomDatabase
import com.opensplit.features.auth.AuthApi
import com.opensplit.features.auth.KtorAuthApi
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.features.expense.KtorExpenseApi
import com.opensplit.features.household.HouseholdApi
import com.opensplit.features.household.KtorHouseholdApi
import com.opensplit.ktor.createHttpClient
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.HouseholdRepository
import com.opensplit.sync.SyncApi
import com.opensplit.sync.SyncManager
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appModule() = module { includes(othersModule(), decomposeModule(), platformModule()) }

fun othersModule() = module {
  factoryOf(::createHttpClient)
  singleOf(::createDataStore)
  singleOf(::DataStoreTokenStorage).bind<TokenStorage>()
  factoryOf(::KtorAuthApi).bind<AuthApi>()
  factoryOf(::KtorHouseholdApi).bind<HouseholdApi>()
  factoryOf(::KtorExpenseApi).bind<ExpenseApi>()
  factoryOf(::SyncApi)

  single {
    getRoomDatabase(
        get<AppDatabaseBuilderFactory>().createBuilder(get()),
        kotlinx.coroutines.Dispatchers.Default,
    )
  }
  single { get<AppDatabase>().householdDao() }
  single { get<AppDatabase>().expenseDao() }
  single { get<AppDatabase>().syncQueueDao() }
  single { get<AppDatabase>().syncMetadataDao() }

  singleOf(::HouseholdRepository)
  singleOf(::ExpenseRepository)
  singleOf(::SyncManager)
}
