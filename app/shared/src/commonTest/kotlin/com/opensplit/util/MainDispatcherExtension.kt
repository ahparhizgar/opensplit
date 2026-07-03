package com.opensplit.util

import io.kotest.core.extensions.SpecExtension
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeTestListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherExtension(private val dispatcher: TestDispatcher = StandardTestDispatcher()) :
    BeforeTestListener, AfterTestListener, SpecExtension {
  constructor(
      testCoroutineScheduler: TestCoroutineScheduler
  ) : this(
      StandardTestDispatcher(
          testCoroutineScheduler,
      ),
  )

  override suspend fun intercept(spec: Spec, execute: suspend (Spec) -> Unit) {
    spec.coroutineTestScope = true
    super.intercept(spec, execute)
  }

  override suspend fun beforeTest(testCase: TestCase) {
    if (testCase.parent == null) {
      Dispatchers.setMain(dispatcher)
    }
  }

  override suspend fun afterTest(
      testCase: TestCase,
      result: TestResult,
  ) {
    if (testCase.parent == null) {
      Dispatchers.resetMain()
    }
  }
}
