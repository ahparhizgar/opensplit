package com.opensplit.di

import com.opensplit.datastore.DataStoreTokenStorage
import com.opensplit.datastore.createDataStore
import com.opensplit.features.auth.AuthApi
import com.opensplit.features.auth.KtorAuthApi
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.features.expense.KtorExpenseApi
import com.opensplit.features.household.HouseholdApi
import com.opensplit.features.household.KtorHouseholdApi
import com.opensplit.ktor.createHttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appModule() = module { includes(othersModule(), decomposeModule()) }

fun othersModule() = module {
  factoryOf(::createHttpClient)
  singleOf(::createDataStore)
  singleOf(::DataStoreTokenStorage).bind<TokenStorage>()
  factoryOf(::KtorAuthApi).bind<AuthApi>()
  factoryOf(::KtorHouseholdApi).bind<HouseholdApi>()
  factoryOf(::KtorExpenseApi).bind<ExpenseApi>()
}
