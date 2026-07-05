package com.opensplit.util

import com.ahparhizgar.katch.NetworkError
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

interface FakeService {
  var errorToThrow: Exception?

  fun disconnect() {
    errorToThrow = NetworkError("Network is mocked as disconnected by errorToThrowField.")
  }

  fun connect() {
    clearError()
  }

  fun clearError() {
    errorToThrow = null
  }

  suspend fun <T> fakeApiCall(block: suspend () -> T): T {
    delay(100.milliseconds)
    if (errorToThrow != null) {
      throw errorToThrow!!
    }
    return block()
  }
}

suspend fun <T> fakeApiCall(block: suspend () -> T): T {
  delay(100.milliseconds)
  return block()
}
