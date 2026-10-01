package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.integration.auth.TokenStorage
import com.opensplit.integration.component.assertSignUp
import com.opensplit.integration.component.assertWelcome
import com.opensplit.repository.ProfileRepository
import com.opensplit.root.RootComponentFactory
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SignUpFlowE2eTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())

    Given("unauthenticated user on cold start") {
      val koin by integrationKoin()
      val context by testValue { TestCContext() }
      val tokenStorage by testValue { koin.get<TokenStorage>() }
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      var root by testValue { koin.get<RootComponentFactory>().create(context) }

      When("waiting on cold start") {
        beforeEach { testCoroutineScheduler.advanceUntilIdle() }

        Then("shows auth welcome screen") { root.assertAuth().assertWelcome() }

        And("clicking sign up") {
          beforeEach { root.assertAuth().assertWelcome().onSignUpClicked() }

          Then("shows sign up screen") { root.assertAuth().assertSignUp() }

          And("submitting valid credentials") {
            beforeEach {
              with(root.assertAuth().assertSignUp()) {
                onEmailChanged("newuser@example.com")
                onPasswordChanged("securePassword123")
                onDoneClicked()
              }
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("token is stored and profile is persisted") {
              tokenStorage.getAccessToken().shouldNotBeNull()
              profileRepo.profile.value?.email shouldBe "newuser@example.com"
            }

            And("navigates to MyGroupsListComponent") {
              Then("active component is MyGroupsListComponent") { root.assertMyGroupsList() }
            }
          }
        }
      }
    }
  }
}
