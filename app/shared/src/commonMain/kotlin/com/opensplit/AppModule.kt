package com.opensplit

import com.opensplit.datastore.DataStoreTokenStorage
import com.opensplit.datastore.createDataStore
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.auth.createAuthGateway
import com.opensplit.features.household.DefaultHouseholdComponent
import com.opensplit.features.household.HouseholdComponent
import com.opensplit.features.household.HouseholdGateway
import com.opensplit.features.household.KtorHouseholdGateway
import com.opensplit.root.ComponentProvider
import com.opensplit.root.DefaultRootComponent
import com.opensplit.root.KoinComponentProvider
import com.opensplit.root.RootComponent
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appModule() = module {
    factory {
        KoinComponentProvider(this)
    }.bind<ComponentProvider>()
    single { createDataStore() }
    singleOf(::DataStoreTokenStorage).bind<TokenStorage>()
    factory { createAuthGateway() }.bind<AuthGateway>()
    factoryOf(::KtorHouseholdGateway).bind<HouseholdGateway>()
    factoryOf(::DefaultRootComponent).bind<RootComponent>()
    factoryOf(::DefaultAuthComponent).bind<AuthComponent>()
    factoryOf(::DefaultHouseholdComponent).bind<HouseholdComponent>()
}

