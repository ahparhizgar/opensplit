package com.opensplit

import androidx.compose.material3.SnackbarResult
import com.opensplit.usermessage.MessageHolder
import com.opensplit.usermessage.SnackbarMessage
import com.opensplit.util.testValue
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.engine.coroutines.backgroundScope
import io.kotest.engine.coroutines.testScheduler
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MessageHolderTest : BehaviorSpec() {
  init {
    coroutineTestScope = true
    Given("a message holder") {
      val messageHolder by testValue { MessageHolder() }
      When("showing a message for result") {
        val message = SnackbarMessage(content = "Test message")
        val result by testValue { async { messageHolder.showSnackbarForResult(message) } }

        var ran by testValue { false }
        val job by testValue {
          backgroundScope.launch {
            messageHolder.showOne {
              delay(2.seconds)
              ran = true
              SnackbarResult.Dismissed
            }
          }
        }
        Then("result is received") { result.await() shouldBe SnackbarResult.Dismissed }
        And("scope is canceled before showing is completed") {
          beforeEach {
            delay(1.seconds)
            result.cancel()
          }
          Then("no exception is thrown and job is completed") { job.isCompleted shouldBe true }
          Then("showing should be canceled") {
            testScheduler.advanceUntilIdle()
            withClue("ran") { ran shouldBe false }
          }
        }
      }

      When("showing a message without result and cancelling") {
        val message = SnackbarMessage(content = "Test message")
        var ran by testValue { false }
        val result by testValue { async { messageHolder.showSnackbar(message) } }
        beforeEach {
          backgroundScope.launch {
            messageHolder.showOne {
              delay(2.seconds)
              ran = true
              SnackbarResult.Dismissed
            }
          }
          delay(1.seconds)
          result.cancel()
        }
        Then("shows the message") {
          delay(3.seconds)
          ran shouldBe true
        }
      }
    }
  }
}
