package com.opensplit

import com.opensplit.datastore.DataStoreTokenStorage
import com.opensplit.datastore.createDataStore
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.auth.createAuthGateway
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.createjoin.DefaultCreateJoinHouseholdComponent
import com.opensplit.features.household.root.DefaultRootHouseholdComponent
import com.opensplit.features.household.details.DefaultHouseholdDetailsComponent
import com.opensplit.features.household.my.DefaultMyHouseholdsListComponent
import com.opensplit.features.household.root.RootHouseholdComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.features.household.HouseholdService
import com.opensplit.features.household.KtorHouseholdService
import com.opensplit.features.household.my.MyHouseholdsListComponent
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
    singleOf(::createDataStore)
    singleOf(::DataStoreTokenStorage).bind<TokenStorage>()
    factory { createAuthGateway() }.bind<AuthGateway>()
    factoryOf(::KtorHouseholdService).bind<HouseholdService>()
    factoryOf(DefaultRootComponent::Factory)
        .bind<RootComponent.Factory>()
    factoryOf(DefaultAuthComponent::Factory)
        .bind<AuthComponent.Factory>()
    factoryOf(DefaultRootHouseholdComponent::Factory)
        .bind<RootHouseholdComponent.Factory>()
    factoryOf(DefaultCreateJoinHouseholdComponent::Factory)
        .bind<CreateJoinHouseholdComponent.Factory>()
    factoryOf(DefaultMyHouseholdsListComponent::Factory)
        .bind<MyHouseholdsListComponent.Factory>()
    factoryOf(DefaultHouseholdDetailsComponent::Factory)
        .bind<HouseholdDetailsComponent.Factory>()
}
