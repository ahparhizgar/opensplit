package com.opensplit.validation.expense

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpenseValidationTest {
  @Test
  fun acceptsValidTitleAndAmount() {
    val result = ExpenseValidation.validateExpense("Dinner", 100.0)

    assertTrue(result.isValid)
    assertTrue(result.errors.isEmpty())
  }

  @Test
  fun rejectsBlankTitle() {
    val result = ExpenseValidation.validateExpense("   ", 100.0)

    assertFalse(result.isValid)
    assertEquals("Title is required", result.errors["title"])
  }

  @Test
  fun rejectsTitleLongerThan255Characters() {
    val result = ExpenseValidation.validateExpense("a".repeat(256), 100.0)

    assertFalse(result.isValid)
    assertEquals("Title is too long (max 255 characters)", result.errors["title"])
  }

  @Test
  fun acceptsTitleOfExactly255Characters() {
    val result = ExpenseValidation.validateExpense("a".repeat(255), 100.0)

    assertTrue(result.isValid)
  }

  @Test
  fun rejectsZeroAndNegativeAmounts() {
    assertEquals(
        "Amount must be greater than zero",
        ExpenseValidation.validateExpense("Dinner", 0.0).errors["amount"],
    )
    assertEquals(
        "Amount must be greater than zero",
        ExpenseValidation.validateExpense("Dinner", -5.0).errors["amount"],
    )
  }

  @Test
  fun rejectsNonFiniteAmounts() {
    assertEquals(
        "Invalid amount",
        ExpenseValidation.validateExpense("Dinner", Double.NaN).errors["amount"],
    )
    assertEquals(
        "Invalid amount",
        ExpenseValidation.validateExpense("Dinner", Double.POSITIVE_INFINITY).errors["amount"],
    )
    assertEquals(
        "Invalid amount",
        ExpenseValidation.validateExpense("Dinner", Double.NEGATIVE_INFINITY).errors["amount"],
    )
  }

  @Test
  fun reportsBothTitleAndAmountErrorsTogether() {
    val result = ExpenseValidation.validateExpense("", 0.0)

    assertFalse(result.isValid)
    assertEquals("Title is required", result.errors["title"])
    assertEquals("Amount must be greater than zero", result.errors["amount"])
  }
}
