package com.opensplit.features.auth

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

fun authModule() = module {
    factoryOf(::AuthService)
}
