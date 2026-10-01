package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.integration.auth.TokenStorage
import com.opensplit.root.RootComponentFactory
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler

class AutoLoginStartupFlowTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())

    Given("user has stored valid auth token") {
      val koin by integrationKoin()
      val tokenStorage by testValue { koin.get<TokenStorage>() }
      val context by testValue { TestCContext() }

      beforeEach { tokenStorage.saveAccessToken("valid-test-token") }

      When("app cold starts") {
        val root by testValue { koin.get<RootComponentFactory>().create(context) }

        Then("navigates directly to MyGroupsListComponent bypassing auth") {
          testCoroutineScheduler.advanceUntilIdle()
          root.assertMyGroupsList()
        }
      }
    }
  }
}
