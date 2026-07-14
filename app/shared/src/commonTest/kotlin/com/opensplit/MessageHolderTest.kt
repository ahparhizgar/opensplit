package com.opensplit

import androidx.compose.material3.SnackbarResult
import com.opensplit.usermessage.MessageHolder
import com.opensplit.usermessage.SnackbarMessage
import com.opensplit.util.testValue
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.scopes.BehaviorSpecGivenContainerScope
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.engine.coroutines.backgroundScope
import io.kotest.engine.coroutines.testScheduler
import io.kotest.matchers.shouldBe
import kotlin.properties.ReadWriteProperty
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MessageHolderTest : BehaviorSpec() {
  init {
    coroutineTestScope = true
    Given("a message holder") {
      val kit by testKit()

      When("showing a message for result") {
        val message = SnackbarMessage(content = "Test message")
        val result by testValue { async { kit.messageHolder.showSnackbarForResult(message) } }

        Then("result is received") {
          testCoroutineScheduler.advanceUntilIdle()
          result.await() shouldBe SnackbarResult.Dismissed
        }
        And("scope is canceled before showing is completed") {
          beforeEach {
            delay(1.seconds)
            result.cancel()
          }
          Then("showing should be canceled") {
            testScheduler.advanceUntilIdle()
            withClue("ran") { kit.showLog shouldBe listOf("start:Test message") }
          }
        }
      }

      When("showing a message without result and cancelling") {
        val message = SnackbarMessage(content = "Test message")
        beforeEach { async { kit.messageHolder.showSnackbar(message) } }
        Then("shows the message") {
          delay(3.seconds)
          kit.showLog shouldBe listOf("start:Test message", "end:Test message")
        }
      }
    }

    Given("using showAll and showSnackbarForResult") {
      val kit by testKit()
      When("showing and cancelling a message and showing another message") {
        beforeEach {
          val message1 = SnackbarMessage(content = "m1")
          val message2 = SnackbarMessage(content = "m2")
          val m1 = launch { kit.messageHolder.showSnackbarForResult(message1) }
          delay(1.seconds)
          m1.cancel()
          launch { kit.messageHolder.showSnackbarForResult(message2) }
          delay(3.seconds)
        }
        Then("the first message is canceled and the second is shown") {
          kit.showLog shouldBe listOf("start:m1", "start:m2", "end:m2")
        }
      }

      When("showing a message then showing the same message") {
        beforeEach {
          val message1 = SnackbarMessage(content = "m1")
          launch { kit.messageHolder.showSnackbarForResult(message1) }
          delay(1.seconds)
          launch { kit.messageHolder.showSnackbarForResult(message1) }
          delay(3.seconds)
        }
        Then("the first message is shown but not completed, the second is shown after") {
          kit.showLog shouldBe listOf("start:m1", "start:m1", "end:m1")
        }
      }
    }

    Given("suing showAll and showSnackbar") {
      val kit by testKit()

      When("showing and cancelling a message and showing another message") {
        beforeEach {
          val message1 = SnackbarMessage(content = "m1")
          val message2 = SnackbarMessage(content = "m2")
          val m1 = launch { kit.messageHolder.showSnackbar(message1) }
          delay(1.seconds)
          m1.cancel()
          delay(3.seconds)
          launch { kit.messageHolder.showSnackbar(message2) }
          delay(3.seconds)
        }
        Then("the first message is canceled and the second is shown") {
          kit.showLog shouldBe listOf("start:m1", "end:m1", "start:m2", "end:m2")
        }
      }
      When("showing a message then showing the same message") {
        beforeEach {
          val message1 = SnackbarMessage(content = "m1")
          launch { kit.messageHolder.showSnackbar(message1) }
          delay(1.seconds)
          launch { kit.messageHolder.showSnackbar(message1) }
          delay(3.seconds)
        }
        Then("the first message is shown but not completed, the second is shown after") {
          kit.showLog shouldBe listOf("start:m1", "start:m1", "end:m1")
        }
      }
    }
  }
}

private fun BehaviorSpecGivenContainerScope.testKit():
    ReadWriteProperty<Any?, MessageHolderTestKit> {
  val kitRef = testValue { MessageHolderTestKit(MessageHolder(), mutableListOf<String>()) }
  beforeEach {
    val kit by kitRef
    val showLog = kit.showLog as MutableList<String>
    backgroundScope.launch {
      kit.messageHolder.showAll {
        showLog.add("start:${it.content}")
        delay(2.seconds)
        showLog.add("end:${it.content}")
        SnackbarResult.Dismissed
      }
    }
  }
  return kitRef
}

private class MessageHolderTestKit(
    val messageHolder: MessageHolder,
    val showLog: List<String> = mutableListOf(),
)
