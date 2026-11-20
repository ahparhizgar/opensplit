package com.opensplit.util

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch

class UtilsTest : BehaviorSpec() {
  init {
    Given("an amount to display") {
      When("formatting a whole amount") {
        Then("it always shows two decimals") {
          20.0.formatAmount() shouldBe "20.00"
          100.0.formatAmount() shouldBe "100.00"
          0.0.formatAmount() shouldBe "0.00"
        }
      }

      When("formatting an amount with one decimal") {
        Then("it pads the second decimal with a zero") {
          15.5.formatAmount() shouldBe "15.50"
          0.1.formatAmount() shouldBe "0.10"
        }
      }

      When("formatting an amount with two decimals") {
        Then("it shows the amount unchanged") {
          10.15.formatAmount() shouldBe "10.15"
          1234567.89.formatAmount() shouldBe "1234567.89"
          0.01.formatAmount() shouldBe "0.01"
        }
      }

      When("formatting an amount with more than two decimals") {
        Then("it rounds down below half a cent") {
          10.154.formatAmount() shouldBe "10.15"
          3.14159.formatAmount() shouldBe "3.14"
        }

        Then("it rounds up above half a cent") { 10.156.formatAmount() shouldBe "10.16" }

        Then("it carries over when rounding up past a whole number") {
          999.999.formatAmount() shouldBe "1000.00"
        }

        Then("a half cent rounds to even, not up") {
          // 0.125 is exact in binary, so round(12.5) is 12.0: halves round to even.
          0.125.formatAmount() shouldBe "0.12"
        }

        Then("a sub-cent amount rounds to zero") {
          // 0.005 is stored slightly below 0.005 in binary, so it rounds down.
          0.005.formatAmount() shouldBe "0.00"
        }
      }

      When("formatting a negative amount") {
        Then("it keeps the minus sign and two decimals") {
          (-5.5).formatAmount() shouldBe "-5.50"
          (-10.156).formatAmount() shouldBe "-10.16"
        }
      }
    }

    Given("a random id") {
      When("generating one") {
        Then("it is a number between 0 and 999999") {
          val id = randomId()
          id shouldMatch Regex("\\d{1,6}")
          val value = id.toInt()
          (value in 0..999999) shouldBe true
        }
      }
    }
  }
}
