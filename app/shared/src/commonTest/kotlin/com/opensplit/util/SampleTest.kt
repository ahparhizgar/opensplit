package com.opensplit.util

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.scopes.BehaviorSpecGivenContainerScope
import io.kotest.core.spec.style.scopes.BehaviorSpecWhenContainerScope
import io.kotest.matchers.shouldBe
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

var logs = ""

class SampleTest :
    BehaviorSpec({
      Given("my var") {
        var myVar by testValue { 1 }
        When("set it to 2") {
          beforeEach { myVar = 2 }
          Then("should be 2") { myVar shouldBe 2 }
          And("adding 3 more") {
            beforeEach { myVar += 3 }
            Then("should be 3") { myVar shouldBe 5 }
          }
        }

        When("adding one") {
          beforeEach { myVar += 1 }
          Then("should be 2") {
            myVar shouldBe 2
            println(logs)
          }
        }
      }
    })

class SampleTestWrong :
    BehaviorSpec({
      Given("a view model") {
        var myVar = 1
        When("first when") {
          myVar = 2
          Then("first then 2") { myVar shouldBe 2 }
        }

        When("second when") {
          myVar += 1
          Then("second then") {
            myVar shouldBe 3 // not 2
            println(logs)
          }
        }
      }
    })

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

fun print(s: String) {
  logs += (s + "\n")
}
