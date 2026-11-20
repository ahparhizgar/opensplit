package com.opensplit.integration.auth

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Clock

class JwtUtilsTest : BehaviorSpec() {
  init {
    Given("a token whose expiry is in the future") {
      When("checking a token expiring an hour from now") {
        Then("it is not expired") {
          isJwtExpired(makeToken("""{"exp":${nowSeconds() + 3600}}""")) shouldBe false
        }
      }

      When("checking a token expiring a minute from now") {
        Then("it is not expired") {
          isJwtExpired(makeToken("""{"exp":${nowSeconds() + 60}}""")) shouldBe false
        }
      }

      When("checking a token with a fixed far-future expiry") {
        Then("it is not expired") {
          // 4102444800 is 2100-01-01T00:00:00Z.
          isJwtExpired(makeToken("""{"exp":4102444800}""")) shouldBe false
        }
      }

      When("checking a token with an empty signature part") {
        Then("only the payload decides, so it is not expired") {
          val header = base64UrlEncode("""{"alg":"HS256","typ":"JWT"}""".encodeToByteArray())
          val payload = base64UrlEncode("""{"exp":4102444800}""".encodeToByteArray())
          isJwtExpired("$header.$payload.") shouldBe false
        }
      }

      When("checking a token whose payload uses URL-safe base64 characters") {
        Then("it is decoded and not expired") {
          // The "~" claim value makes the base64url payload contain a '-',
          // so this exercises the '-'/'_' to '+'/'/' replacement.
          isJwtExpired(makeToken("""{"exp":4102444800,"name":"~"}""")) shouldBe false
        }
      }
    }

    Given("a token whose expiry has passed") {
      When("checking a token that expired an hour ago") {
        Then("it is expired") {
          isJwtExpired(makeToken("""{"exp":${nowSeconds() - 3600}}""")) shouldBe true
        }
      }

      When("checking a token that expired a minute ago") {
        Then("it is expired") {
          isJwtExpired(makeToken("""{"exp":${nowSeconds() - 60}}""")) shouldBe true
        }
      }

      When("checking a token with a fixed long-past expiry") {
        Then("it is expired") { isJwtExpired(makeToken("""{"exp":1}""")) shouldBe true }
      }

      When("checking a token expiring exactly now") {
        Then("it is expired, because expiry is inclusive") {
          isJwtExpired(makeToken("""{"exp":${nowSeconds()}}""")) shouldBe true
        }
      }
    }

    Given("a token without a usable expiry claim") {
      When("checking a token with no exp claim") {
        Then("it is expired") { isJwtExpired(makeToken("""{"sub":"user-1"}""")) shouldBe true }
      }

      When("checking a token with a null exp claim") {
        Then("it is expired") { isJwtExpired(makeToken("""{"exp":null}""")) shouldBe true }
      }

      When("checking a token with a non-numeric string exp claim") {
        Then("it is expired") { isJwtExpired(makeToken("""{"exp":"tomorrow"}""")) shouldBe true }
      }

      When("checking a token with a numeric string exp claim") {
        Then("it is parsed as a number, so it is not expired") {
          // JsonPrimitive.longOrNull parses numeric strings, so a string exp
          // that holds a future epoch is treated like a numeric exp.
          isJwtExpired(makeToken("""{"exp":"4102444800"}""")) shouldBe false
        }
      }

      When("checking a token whose payload is not JSON") {
        Then("it is expired") { isJwtExpired(makeToken("not-json")) shouldBe true }
      }
    }

    Given("a malformed token") {
      When("checking an empty token") { Then("it is expired") { isJwtExpired("") shouldBe true } }

      When("checking a token with no dots") {
        Then("it is expired") { isJwtExpired("abc") shouldBe true }
      }

      When("checking a token with only two parts") {
        Then("it is expired") { isJwtExpired("header.payload") shouldBe true }
      }

      When("checking a token with four parts") {
        Then("it is expired") { isJwtExpired("a.b.c.d") shouldBe true }
      }

      When("checking a token with an invalid base64 payload") {
        Then("it is expired") { isJwtExpired("header.!!!.signature") shouldBe true }
      }
    }
  }
}

/** Current time in seconds, via the stdlib clock. */
private fun nowSeconds(): Long = Clock.System.now().epochSeconds

/** Builds an unsigned token with the given JSON payload, for testing only. */
private fun makeToken(payloadJson: String): String {
  val header = base64UrlEncode("""{"alg":"HS256","typ":"JWT"}""".encodeToByteArray())
  val payload = base64UrlEncode(payloadJson.encodeToByteArray())
  return "$header.$payload.signature"
}

/** Minimal base64url encoder without padding, for building test tokens. */
private fun base64UrlEncode(bytes: ByteArray): String {
  val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
  val out = StringBuilder()
  var i = 0
  while (i < bytes.size) {
    val b0 = bytes[i].toInt() and 0xFF
    val b1 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else 0
    val b2 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else 0
    out.append(alphabet[b0 shr 2])
    out.append(alphabet[((b0 and 0x03) shl 4) or (b1 shr 4)])
    if (i + 1 < bytes.size) out.append(alphabet[((b1 and 0x0F) shl 2) or (b2 shr 6)])
    if (i + 2 < bytes.size) out.append(alphabet[b2 and 0x3F])
    i += 3
  }
  return out.toString()
}
