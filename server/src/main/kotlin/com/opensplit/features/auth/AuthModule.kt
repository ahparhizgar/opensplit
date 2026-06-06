package com.opensplit.features.auth

import org.koin.dsl.module

fun authModule() = module {
    factory { AuthService() }
}
