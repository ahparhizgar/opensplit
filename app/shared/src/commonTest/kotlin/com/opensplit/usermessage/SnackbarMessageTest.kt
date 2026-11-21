package com.opensplit.usermessage

import androidx.compose.material3.SnackbarDuration
import com.opensplit.usermessage.UserMessage.DismissMode
import com.opensplit.usermessage.UserMessage.Duration
import com.opensplit.usermessage.UserMessage.Tone
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SnackbarMessageTest : BehaviorSpec() {
  init {
    Given("a snackbar message with default settings") {
      val message = SnackbarMessage(content = "Hello")

      Then("it exposes the content as its message") { message.message shouldBe "Hello" }

      Then("its tone defaults to Info") { message.tone shouldBe Tone.Info }

      Then("its dismiss mode defaults to auto and manual with a short duration") {
        message.dismissMode shouldBe DismissMode.AutoAndManual(Duration.Short)
      }

      Then("it can be dismissed manually") { message.withDismissAction shouldBe true }

      Then("it disappears after a short duration") {
        message.duration shouldBe SnackbarDuration.Short
      }

      Then("it has no second action label") { message.actionLabel2 shouldBe null }

      Then("it does not shake by default") { message.shouldShake shouldBe false }

      Then("it has no action label by default") { message.actionLabel shouldBe null }
    }

    Given("a snackbar message with a manual dismiss mode") {
      val message = SnackbarMessage(content = "Hello", dismissMode = DismissMode.Manual)

      Then("it can be dismissed manually") { message.withDismissAction shouldBe true }

      Then("it stays on screen until dismissed") {
        message.duration shouldBe SnackbarDuration.Indefinite
      }
    }

    Given("a snackbar message with an auto-only dismiss mode") {
      When("the duration is short") {
        val message =
            SnackbarMessage(content = "Hello", dismissMode = DismissMode.AutoOnly(Duration.Short))

        Then("it cannot be dismissed manually") { message.withDismissAction shouldBe false }

        Then("it disappears after a short duration") {
          message.duration shouldBe SnackbarDuration.Short
        }
      }

      When("the duration is long") {
        val message =
            SnackbarMessage(content = "Hello", dismissMode = DismissMode.AutoOnly(Duration.Long))

        Then("it cannot be dismissed manually") { message.withDismissAction shouldBe false }

        Then("it disappears after a long duration") {
          message.duration shouldBe SnackbarDuration.Long
        }
      }
    }

    Given("a snackbar message with an auto-and-manual dismiss mode") {
      When("the duration is long") {
        val message =
            SnackbarMessage(
                content = "Hello",
                dismissMode = DismissMode.AutoAndManual(Duration.Long),
            )

        Then("it can be dismissed manually") { message.withDismissAction shouldBe true }

        Then("it disappears after a long duration") {
          message.duration shouldBe SnackbarDuration.Long
        }
      }
    }

    Given("a snackbar message with a second action label") {
      val message =
          SnackbarMessage(content = "Hello", actionLabel2 = UiString.StringLiteral("Retry"))

      Then("it keeps the label") { message.actionLabel2 shouldBe UiString.StringLiteral("Retry") }
    }

    Given("a snackbar message whose action label is set") {
      val message = SnackbarMessage(content = "Hello")

      When("the action label is assigned") {
        message.actionLabel = "Undo"
        Then("it keeps the label") { message.actionLabel shouldBe "Undo" }
      }

      When("shaking is enabled") {
        message.shouldShake = true
        Then("it shakes") { message.shouldShake shouldBe true }
      }
    }

    Given("a user message") {
      val message = UserMessage(content = UiString.StringLiteral("Hello"))

      Then("its tone defaults to Info") { message.tone shouldBe Tone.Info }

      Then("its dismiss mode defaults to auto and manual with a short duration") {
        message.dismissMode shouldBe DismissMode.AutoAndManual(Duration.Short)
      }
    }
  }
}
