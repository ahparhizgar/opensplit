package com.opensplit.di

import com.opensplit.datastore.DataStoreTokenStorage
import com.opensplit.datastore.createDataStore
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.KtorAuthGateway
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.household.HouseholdService
import com.opensplit.features.household.KtorHouseholdService
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
  factoryOf(::KtorAuthGateway).bind<AuthGateway>()
  factoryOf(::KtorHouseholdService).bind<HouseholdService>()
}
