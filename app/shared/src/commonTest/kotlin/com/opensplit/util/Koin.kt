package com.opensplit.util

import com.opensplit.DataDir
import com.opensplit.db.AppDatabaseBuilderFactory
import com.opensplit.db.getRoomDatabase
import com.opensplit.di.appModule
import com.opensplit.fake.FakeAuthApi
import com.opensplit.fake.FakeExpenseApi
import com.opensplit.fake.FakeHouseholdApi
import com.opensplit.fake.FakeSyncApi
import com.opensplit.features.auth.AuthApi
import com.opensplit.features.auth.NoOpTokenStorage
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.expense.ExpenseApi
import com.opensplit.features.household.HouseholdApi
import com.opensplit.repository.InMemoryProfileRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.sync.SyncApi
import io.kotest.core.spec.Spec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun Spec.integrationKoin() = testValue {
  koinApplication {
        allowOverride(true)
        modules(
            appModule(),
            integrationTestModule(),
        )
      }
      .koin
}

fun uiKoin() =
    koinApplication {
          allowOverride(true)
          modules(
              appModule(),
              integrationTestModule(),
          )
        }
        .koin

fun integrationTestModule() = module {
  single { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
  single { DataDir.MEMORY }
  single {
    getRoomDatabase(
        get<AppDatabaseBuilderFactory>().createBuilder(dataDir = get()),
        Dispatchers.Unconfined,
    )
  }
  single { FakeAuthApi() }.bind<AuthApi>()
  single { NoOpTokenStorage() }.bind<TokenStorage>()
  single { InMemoryProfileRepository() }.bind<ProfileRepository>()
  single { FakeHouseholdApi() }.bind<HouseholdApi>()
  single { FakeExpenseApi() }.bind<ExpenseApi>()
  single { FakeSyncApi() }.bind<SyncApi>()
}
