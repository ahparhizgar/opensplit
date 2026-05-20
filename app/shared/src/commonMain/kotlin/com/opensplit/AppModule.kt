package com.opensplit

import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.features.auth.createAuthGateway
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appModule() = module {
    factory { createAuthGateway() }
    factoryOf(DefaultAuthComponent::Factory).bind<AuthComponent.Factory>()
}
