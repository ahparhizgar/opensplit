package com.opensplit.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CurrencyTest : BehaviorSpec() {
  init {
    Given("the default currency") {
      Then("it is IRR, the currency every group uses today") {
        Currency.default shouldBe Currency.IRR
      }
    }

    Given("an ISO currency code") {
      When("looking it up with fromCode") {
        Then("it resolves case-insensitively") {
          Currency.fromCode("usd") shouldBe Currency.USD
          Currency.fromCode("EUR") shouldBe Currency.EUR
          Currency.fromCode("irr") shouldBe Currency.IRR
        }

        Then("unknown codes return null") {
          Currency.fromCode("XXX") shouldBe null
          Currency.fromCode("") shouldBe null
        }
      }

      When("serializing a currency to JSON") {
        Then("it encodes as its enum name, ready for use in DTOs") {
          Json.encodeToString(Currency.USD) shouldBe "\"USD\""
          Json.decodeFromString<Currency>("\"EUR\"") shouldBe Currency.EUR
        }
      }
    }

    Given("an amount to display") {
      When("formatting it in IRR") {
        Then("it uses the IRR prefix and two decimals, like the UI showed before") {
          Currency.IRR.format(10.0) shouldBe "IRR10.00"
          Currency.IRR.format(10.15) shouldBe "IRR10.15"
          Currency.IRR.format(0.0) shouldBe "IRR0.00"
        }
      }

      When("formatting it in USD") {
        Then("it uses the dollar sign and two decimals") {
          Currency.USD.format(10.0) shouldBe "$10.00"
          Currency.USD.format(10.156) shouldBe "$10.16"
        }
      }

      When("formatting it in JPY, a zero-decimal currency") {
        Then("it shows no decimals") {
          Currency.JPY.format(10.0) shouldBe "¥10"
          Currency.JPY.format(10.6) shouldBe "¥11"
        }
      }
    }
  }
}
