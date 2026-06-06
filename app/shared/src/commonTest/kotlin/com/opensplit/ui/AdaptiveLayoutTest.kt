package com.opensplit.ui

import androidx.compose.ui.unit.dp
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class AdaptiveLayoutTest : BehaviorSpec({
    Given("adaptive layout width breakpoints") {
        Then("treats narrow widths as compact") {
            adaptiveLayoutSizeClass(599.dp) shouldBe AdaptiveLayoutSizeClass.Compact
        }

        Then("treats medium widths as medium") {
            adaptiveLayoutSizeClass(600.dp) shouldBe AdaptiveLayoutSizeClass.Medium
            adaptiveLayoutSizeClass(839.dp) shouldBe AdaptiveLayoutSizeClass.Medium
        }

        Then("treats wide widths as expanded") {
            adaptiveLayoutSizeClass(840.dp) shouldBe AdaptiveLayoutSizeClass.Expanded
        }
    }
})

