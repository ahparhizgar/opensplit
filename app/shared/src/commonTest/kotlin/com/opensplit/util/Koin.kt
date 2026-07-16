package com.opensplit.util

import com.opensplit.di.appModule
import com.opensplit.fake.FakeAuthService
import com.opensplit.fake.FakeHouseholdService
import com.opensplit.features.auth.AuthService
import com.opensplit.features.auth.NoOpTokenStorage
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.household.HouseholdService
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
  single { FakeAuthService() }.bind<AuthService>()
  single { NoOpTokenStorage() }.bind<TokenStorage>()
  single { FakeHouseholdService() }.bind<HouseholdService>()
}
