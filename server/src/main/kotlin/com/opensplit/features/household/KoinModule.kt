package com.opensplit.features.household

import org.koin.dsl.module

fun householdKoinModule() = module {
  single<HouseholdRepository> { HouseholdRepositoryImpl(get(), get()) }
  single { HouseholdService(get()) }
}
