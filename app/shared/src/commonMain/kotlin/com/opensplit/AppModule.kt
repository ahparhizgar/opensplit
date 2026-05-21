package com.opensplit

import com.opensplit.datastore.DataStoreTokenStorage
import com.opensplit.datastore.createDataStore
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.features.auth.NoOpTokenStorage
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.auth.createAuthGateway
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appModule() = module {
    factoryOf(::DataStoreTokenStorage)
    single { createDataStore() }
    factory { createAuthGateway() }.bind<AuthGateway>()
    singleOf(::DataStoreTokenStorage).bind<TokenStorage>()
    factoryOf(DefaultAuthComponent::Factory).bind<AuthComponent.Factory>()
}
