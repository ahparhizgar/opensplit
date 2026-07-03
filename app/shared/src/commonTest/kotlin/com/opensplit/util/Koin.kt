package com.opensplit.util

import com.opensplit.FakeAuthGateway
import com.opensplit.FakeHouseholdService
import com.opensplit.appModule
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.NoOpTokenStorage
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.household.HouseholdService
import io.kotest.core.spec.Spec
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun Spec.integrationKoin() = testValue {
  koinApplication { modules(appModule(), integrationTestModule()) }.koin
}

fun integrationTestModule() = module {
  single { FakeAuthGateway() }.bind<AuthGateway>()
  single { NoOpTokenStorage() }.bind<TokenStorage>()
  single { FakeHouseholdService() }.bind<HouseholdService>()
}
