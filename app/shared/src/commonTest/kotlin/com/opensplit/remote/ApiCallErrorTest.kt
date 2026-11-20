package com.opensplit.remote

import com.ahparhizgar.katch.ApiCallError
import com.ahparhizgar.katch.BadRequest
import com.ahparhizgar.katch.ClientError
import com.ahparhizgar.katch.Forbidden
import com.ahparhizgar.katch.InvalidDataError
import com.ahparhizgar.katch.NetworkError
import com.ahparhizgar.katch.NotFound
import com.ahparhizgar.katch.OtherClientError
import com.ahparhizgar.katch.RateLimitReached
import com.ahparhizgar.katch.ServerError
import com.ahparhizgar.katch.Unauthorized
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/** An [ApiCallError] that is none of the known subtypes, for the fallback branch. */
private class CustomApiError(
    override val message: String? = null,
    override val payload: Any? = null,
) : ApiCallError()

/**
 * Reads the [userMessage] extension property. Errors must be typed as [ApiCallError] for the
 * extension to apply: on a [ClientError] the member property of the same name (the server-provided
 * message, usually null) wins over the extension.
 */
private fun ApiCallError.displayedMessage(): String = userMessage

class ApiCallErrorTest : BehaviorSpec() {
  init {
    Given("an API call error with a payload") {
      When("the payload is a map of field errors") {
        Then("fieldErrors returns it") {
          val error =
              BadRequest(payload = mapOf("email" to "already taken", "password" to "too short"))

          error.fieldErrors shouldBe mapOf("email" to "already taken", "password" to "too short")
        }
      }

      When("the payload is null") {
        Then("fieldErrors is empty") {
          BadRequest().fieldErrors shouldBe emptyMap()
          NetworkError().fieldErrors shouldBe emptyMap()
        }
      }

      When("the payload is not a map") {
        Then("fieldErrors is empty") {
          BadRequest(payload = "plain string").fieldErrors shouldBe emptyMap()
          BadRequest(payload = listOf("email")).fieldErrors shouldBe emptyMap()
        }
      }
    }

    Given("a client error without a server-provided message") {
      When("it is a bad request") {
        Then("the default message is used") {
          BadRequest().displayedMessage() shouldBe "Invalid request"
        }
      }

      When("it is an unauthorized error") {
        Then("the default message is used") {
          Unauthorized().displayedMessage() shouldBe "Unauthorized access"
        }
      }

      When("it is a forbidden error") {
        Then("the default message is used") {
          Forbidden().displayedMessage() shouldBe "Access denied"
        }
      }

      When("it is a not found error") {
        Then("the default message is used") {
          NotFound().displayedMessage() shouldBe "Content not found"
        }
      }

      When("it is a rate limit error") {
        Then("the default message is used") {
          RateLimitReached().displayedMessage() shouldBe "Too many requests"
        }
      }

      When("it is another client error") {
        Then("the generic client message is used") {
          OtherClientError(code = 422).displayedMessage() shouldBe "A client error occurred"
        }
      }
    }

    Given("a client error with a server-provided message") {
      When("a userMessage is present") {
        Then("it wins over the default message") {
          BadRequest(userMessage = "Email is already registered").displayedMessage() shouldBe
              "Email is already registered"
          Unauthorized(userMessage = "Session expired").displayedMessage() shouldBe
              "Session expired"
          OtherClientError(code = 422, userMessage = "Unprocessable").displayedMessage() shouldBe
              "Unprocessable"
        }
      }
    }

    Given("a client error with a debug message") {
      When("reading the user message") {
        Then("no HTTP code suffix is appended, because client errors are not HttpErrors") {
          // ClientError extends ApiCallError directly, not HttpError, so the
          // " (code: message)" debug suffix only applies to server errors.
          BadRequest(message = "validation failed").displayedMessage() shouldBe "Invalid request"
          NotFound(message = "group 42").displayedMessage() shouldBe "Content not found"
        }
      }
    }

    Given("a network error") {
      When("reading the user message") {
        Then("it says the internet is disconnected, whatever the detail message is") {
          NetworkError().displayedMessage() shouldBe "Internet is disconnected"
          NetworkError(message = "connection timed out").displayedMessage() shouldBe
              "Internet is disconnected"
        }
      }
    }

    Given("a server error") {
      When("there is no detail message") {
        Then("the code is appended in parentheses") {
          ServerError(code = 500).displayedMessage() shouldBe "An error occurred on our side (500)"
        }
      }

      When("there is a detail message") {
        Then("the code and message are appended in parentheses") {
          ServerError(code = 503, message = "service unavailable").displayedMessage() shouldBe
              "An error occurred on our side (503: service unavailable)"
        }
      }

      When("the detail message is blank") {
        Then("only the code is appended") {
          ServerError(code = 500, message = "").displayedMessage() shouldBe
              "An error occurred on our side (500)"
          ServerError(code = 500, message = "   ").displayedMessage() shouldBe
              "An error occurred on our side (500)"
        }
      }
    }

    Given("an invalid data error") {
      When("there is no detail message") {
        Then("the default message is used") {
          InvalidDataError().displayedMessage() shouldBe
              "An error occurred during processing of data"
        }
      }

      When("there is a detail message") {
        Then("the detail message is used as-is") {
          InvalidDataError(message = "missing required field").displayedMessage() shouldBe
              "missing required field"
        }
      }
    }

    Given("an error of an unknown type") {
      When("there is no detail message") {
        Then("the unknown-error message is used") {
          CustomApiError().displayedMessage() shouldBe "An unknown error occurred"
        }
      }

      When("there is a detail message") {
        Then("the detail message is used as-is") {
          CustomApiError(message = "something odd happened").displayedMessage() shouldBe
              "something odd happened"
        }
      }
    }
  }
}
