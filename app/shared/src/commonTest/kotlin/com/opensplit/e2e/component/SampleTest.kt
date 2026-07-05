package com.opensplit.e2e.component

import com.arkivanov.decompose.DecomposeSettings
import com.opensplit.assertLogin
import com.opensplit.assertWelcome
import com.opensplit.component.createDefaultComponentContext
import com.opensplit.features.auth.AuthComponent
import com.opensplit.root.RootComponent
import com.opensplit.splash.SplashDestination
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.When
import com.opensplit.util.createComponentContext
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.coroutines.ContinuationInterceptor

class SampleTest :
    BehaviorSpec({
      extensions(MainDispatcherExtension())
      DecomposeSettings.settings =
          DecomposeSettings(
              mainThreadCheckEnabled = false,
              duplicateConfigurationsEnabled = true,
          )
      Given("app opens for first time") {
        val koin by integrationKoin()
        var root by testValue {
          koin
              .get<RootComponent.Factory>()
              .create(createDefaultComponentContext(createComponentContext()))
        }
        Then("shows welcome screen") {
          println(coroutineContext[ContinuationInterceptor.Key])
          root.assertSplash()
        }
        When(
            "waiting",
            { testCoroutineScheduler.advanceUntilIdle() },
        ) {
          Then("shows auth screen") { root.assertAuth().assertWelcome() }
          And(
              "click on login",
          ) {
            beforeEach { root.assertAuth().assertWelcome().onLoginClicked() }
            Then("shows login screen") { root.assertAuth().assertLogin() }
          }
        }
      }
    })

private fun RootComponent.activeInstance(): Any = childStack.value.active.instance

fun RootComponent.assertAuth() = activeInstance().shouldBeInstanceOf<AuthComponent>()

fun RootComponent.assertSplash() = activeInstance().shouldBeInstanceOf<SplashDestination>()
