package com.opensplit.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Asserts that two [Instant]s are equal within a given [tolerance] (defaults to 1 second).
 *
 * Example usage:
 * ```
 * created.lastInteractionAt almostEquals now
 * fetched.lastInteractionAt.almostEquals(created.lastInteractionAt, tolerance = 2.seconds)
 * ```
 */
infix fun Instant.almostEquals(other: Instant) {
  almostEquals(other, tolerance = 1.seconds)
}

fun Instant.almostEquals(other: Instant, tolerance: Duration = 1.seconds) {
  val diff = (this - other).absoluteValue
  if (diff >= tolerance) {
    throw AssertionError(
        "Expected $this to be almost equal to $other (within $tolerance), but difference was $diff"
    )
  }
}
