package com.opensplit.util

import com.opensplit.di.appModule
import com.opensplit.fake.FakeAuthApi
import com.opensplit.fake.FakeHouseholdApi
import com.opensplit.features.auth.AuthApi
import com.opensplit.features.auth.NoOpTokenStorage
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.household.HouseholdApi
import io.kotest.core.spec.Spec
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun Spec.integrationKoin() = testValue {
  koinApplication {
        modules(
            appModule(),
            integrationTestModule(),
        )
      }
      .koin
}

fun uiKoin() =
    koinApplication {
          modules(
              appModule(),
              integrationTestModule(),
          )
        }
        .koin

fun integrationTestModule() = module {
  single { FakeAuthApi() }.bind<AuthApi>()
  single { NoOpTokenStorage() }.bind<TokenStorage>()
  single { FakeHouseholdApi() }.bind<HouseholdApi>()
}
