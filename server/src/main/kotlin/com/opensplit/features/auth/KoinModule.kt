package com.opensplit.features.auth

import com.opensplit.config.JwtConfig
import org.koin.dsl.module

fun authKoinModule() = module {
  single { JwtConfig.fromEnvironment() }
  single<PasswordHasher> { BcryptPasswordHasher(12) }
  single { JwtService(get()) }
  single<AuthRepository> { AuthRepositoryImpl(get()) }
  single { AuthService(get(), get(), get()) }
}

fun testAuthKoinModule() = module { single<PasswordHasher> { BcryptPasswordHasher(4) } }
