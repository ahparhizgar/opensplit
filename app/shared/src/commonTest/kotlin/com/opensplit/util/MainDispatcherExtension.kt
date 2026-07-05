package com.opensplit.util

import io.kotest.core.extensions.SpecExtension
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherExtension(private val dispatcher: TestDispatcher = StandardTestDispatcher()) :
    BeforeSpecListener, AfterSpecListener, SpecExtension {
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

  override suspend fun beforeSpec(spec: Spec) {
    Dispatchers.setMain(dispatcher)
  }

  override suspend fun afterSpec(spec: Spec) {
    Dispatchers.resetMain()
  }
}
