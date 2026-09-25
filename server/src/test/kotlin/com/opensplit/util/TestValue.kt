package com.opensplit.util

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.scopes.BehaviorSpecGivenContainerScope
import io.kotest.core.spec.style.scopes.BehaviorSpecWhenContainerScope
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> Spec.testValue(initializer: suspend () -> T): ReadWriteProperty<Any?, T> =
    registerTestValue({ beforeEach { it() } }, { afterEach { it() } }, initializer)

fun <T> BehaviorSpecGivenContainerScope.testValue(
    initializer: suspend () -> T
): ReadWriteProperty<Any?, T> =
    registerTestValue({ beforeEach { it() } }, { afterEach { it() } }, initializer)

fun <T> BehaviorSpecWhenContainerScope.testValue(
    initializer: suspend () -> T
): ReadWriteProperty<Any?, T> =
    registerTestValue({ beforeEach { it() } }, { afterEach { it() } }, initializer)

private fun <T> registerTestValue(
    onBeforeEach: (suspend () -> Unit) -> Unit,
    onAfterEach: (suspend () -> Unit) -> Unit,
    initializer: suspend () -> T,
): ReadWriteProperty<Any?, T> {
  var isInitialized = false
  var backingValue: T? = null

  onBeforeEach {
    backingValue = initializer()
    isInitialized = true
  }

  onAfterEach {
    backingValue = null
    isInitialized = false
  }

  return object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
      if (!isInitialized) {
        error(
            "Property ${property.name} was accessed in a wrong scope! Please use 'beforeEach' to access it."
        )
      }
      return backingValue!!
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
      if (!isInitialized) {
        error(
            "Property ${property.name} was accessed in a wrong scope! Please use 'beforeEach' to access it."
        )
      }
      backingValue = value
    }
  }
}
