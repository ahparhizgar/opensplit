package com.opensplit.util

import io.kotest.core.names.TestName
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.TestXMethod
import io.kotest.core.spec.style.scopes.BehaviorSpecWhenContainerScope
import io.kotest.core.spec.style.scopes.ContainerScope
import io.kotest.matchers.shouldBe
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

var logs = ""

class SampleTest : BehaviorSpec({
    Given("my var") {
        var myVar by testValue { 1 }
        When(
            "set it to 2",
            { myVar = 2 }
        ) {
            Then("should be 2") {
                myVar shouldBe 2
            }
            When("adding 3 more") {
                beforeEach {
                    myVar += 3
                }
                Then("should be 3") {
                    myVar shouldBe 5
                }
            }
        }

        When("adding one") {
            beforeEach {
                myVar += 1
            }
            Then("should be 2") {
                myVar shouldBe 2
                println(logs)
            }
        }
    }
})

suspend fun ContainerScope.When(
    name: String,
    action: suspend BehaviorSpecWhenContainerScope.() -> Unit,
    test: suspend BehaviorSpecWhenContainerScope.() -> Unit
) =
    registerContainer(
        TestName(
            name = name, prefix = "When: ", focus = false,
            bang = false,
            suffix = null,
            defaultAffixes = true
        ),
        xmethod = TestXMethod.NONE,
        config = null,
    ) {
        with(BehaviorSpecWhenContainerScope(this)) {
            beforeEach {
                action()
            }
            test()
        }
    }

class SampleTestWrong : BehaviorSpec({
    Given("a view model") {
        var myVar = 1
        When("first when") {
            myVar = 2
            Then("first then 2") {
                myVar shouldBe 2
            }
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

fun <T> Spec.testValue(initializer: () -> T): ReadWriteProperty<Any?, T> {
    var isInitialized = false
    var backingValue: T? = null
    beforeEach {
        backingValue = initializer()
        isInitialized = true
    }
    afterEach {
        backingValue = null
        isInitialized = false
    }
    return object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!isInitialized) {
                error(message(property))
            }
            return backingValue!!
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (!isInitialized) {
                error(message(property))
            }
            backingValue = value
        }

        private fun message(property: KProperty<*>): String =
            "Property ${property.name} was accessed in a wrong scope! Please use 'beforeEach' to access it."
    }
}

fun print(s: String) {
    logs += (s + "\n")
}
