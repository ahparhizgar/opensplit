package com.opensplit.usermessage

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.StringResource

@OptIn(InternalResourceApi::class)
class UiStringTest : BehaviorSpec() {
  init {
    Given("creating a UiString from text") {
      When("the text is empty") {
        Then("it is the empty string") { UiString("") shouldBe UiString.EmptyString }
      }

      When("the text is not empty") {
        Then("it is a literal holding the text") {
          UiString("hello") shouldBe UiString.StringLiteral("hello")
        }
      }
    }

    Given("creating a UiString from a string resource") {
      val res = StringResource("test_key", "test value", emptySet())

      When("no format arguments are given") {
        Then("it is a resource string") { UiString(res) shouldBe UiString.StringRes(res) }
      }

      When("format arguments are given") {
        Then("it keeps the resource and the arguments") {
          val uiString = UiString(res, "arg", 42)
          uiString shouldBe UiString.StringResWithArgument(res, arrayOf<Any>("arg", 42))
          (uiString as UiString.StringResWithArgument).res shouldBe res
          uiString.formatArgs shouldBe arrayOf<Any>("arg", 42)
        }
      }
    }

    Given("concatenating two UiStrings") {
      val a = UiString.StringLiteral("a")
      val b = UiString.StringLiteral("b")
      val c = UiString.StringLiteral("c")
      val d = UiString.StringLiteral("d")

      When("both parts are literals") {
        Then("it is a concatenation of both") { (a + b) shouldBe UiString.Concat(listOf(a, b)) }
      }

      When("the left part is empty") {
        Then("it is the right part unchanged") {
          (UiString.EmptyString + a) shouldBe a
          (UiString.EmptyString + UiString.Concat(listOf(a, b))) shouldBe
              UiString.Concat(listOf(a, b))
        }
      }

      When("the right part is empty") {
        Then("it is the left part unchanged") {
          (a + UiString.EmptyString) shouldBe a
          (UiString.Concat(listOf(a, b)) + UiString.EmptyString) shouldBe
              UiString.Concat(listOf(a, b))
        }
      }

      When("both parts are empty") {
        Then("it is the empty string") {
          (UiString.EmptyString + UiString.EmptyString) shouldBe UiString.EmptyString
        }
      }

      When("the left part is already a concatenation") {
        Then("it flattens instead of nesting") {
          ((a + b) + c) shouldBe UiString.Concat(listOf(a, b, c))
        }
      }

      When("the right part is already a concatenation") {
        Then("it flattens instead of nesting") {
          (a + (b + c)) shouldBe UiString.Concat(listOf(a, b, c))
        }
      }

      When("both parts are concatenations") {
        Then("it flattens both") {
          ((a + b) + (c + d)) shouldBe UiString.Concat(listOf(a, b, c, d))
        }
      }

      When("a concatenation contains an empty part") {
        Then("the empty part is dropped") {
          (UiString.Concat(listOf(a, UiString.EmptyString)) + b) shouldBe
              UiString.Concat(listOf(a, b))
        }
      }

      When("only one non-empty part remains") {
        Then("it collapses to that part") {
          (UiString.Concat(listOf(a)) + UiString.Concat(emptyList())) shouldBe a
        }
      }

      When("no non-empty part remains") {
        Then("it collapses to the empty string") {
          (UiString.Concat(emptyList()) + UiString.Concat(emptyList())) shouldBe
              UiString.EmptyString
        }
      }
    }

    Given("concatenating a UiString with plain text") {
      val a = UiString.StringLiteral("a")
      val b = UiString.StringLiteral("b")

      When("text is appended to a literal") {
        Then("it is a concatenation of both") { (a + "b") shouldBe UiString.Concat(listOf(a, b)) }
      }

      When("empty text is appended") {
        Then("it is the original string unchanged") { (a + "") shouldBe a }
      }
    }

    Given("prepending text to a UiString") {
      val b = UiString.StringLiteral("b")

      When("text is prepended through the factory") {
        Then("it is a concatenation of both") {
          // Note: plain `"a" + b` cannot work here: String.plus(Any?) is a member
          // of String and always wins over an extension operator, so text must
          // be wrapped in UiString first.
          (UiString("a") + b) shouldBe UiString.Concat(listOf(UiString.StringLiteral("a"), b))
        }
      }

      When("empty text is prepended through the factory") {
        Then("it is the original string unchanged") { (UiString("") + b) shouldBe b }
      }
    }

    Given("comparing resource strings with arguments") {
      val res = StringResource("test_key", "test value", emptySet())
      val otherRes = StringResource("other_key", "other value", emptySet())

      When("the resource and arguments match, in different arrays") {
        Then("they are equal and share a hash code") {
          // Arrays use reference equality by default, so this only passes
          // because StringResWithArgument overrides equals/hashCode.
          val first = UiString.StringResWithArgument(res, arrayOf<Any>("a", 1))
          val second = UiString.StringResWithArgument(res, arrayOf<Any>("a", 1))
          first shouldBe second
          first.hashCode() shouldBe second.hashCode()
        }
      }

      When("an argument differs") {
        Then("they are not equal") {
          UiString.StringResWithArgument(res, arrayOf<Any>("a", 1)) shouldNotBe
              UiString.StringResWithArgument(res, arrayOf<Any>("b", 1))
        }
      }

      When("the resource differs") {
        Then("they are not equal") {
          UiString.StringResWithArgument(res, arrayOf<Any>("a", 1)) shouldNotBe
              UiString.StringResWithArgument(otherRes, arrayOf<Any>("a", 1))
        }
      }

      When("the other value is a different UiString type") {
        Then("they are not equal") {
          UiString.StringResWithArgument(res, arrayOf<Any>("a")) shouldNotBe
              UiString.StringLiteral("a")
        }
      }
    }
  }
}
