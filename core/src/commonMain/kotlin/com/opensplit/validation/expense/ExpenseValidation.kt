package com.opensplit.validation.expense

import com.opensplit.validation.auth.ValidationResult

object ExpenseValidation {
  fun validateExpense(title: String, amount: Double): ValidationResult {
    val errors = mutableMapOf<String, String>()
    if (title.isBlank()) {
      errors["title"] = "Title is required"
    } else if (title.length > 255) {
      errors["title"] = "Title is too long (max 255 characters)"
    }
    if (amount.isNaN() || amount.isInfinite()) {
      errors["amount"] = "Invalid amount"
    } else if (amount <= 0) {
      errors["amount"] = "Amount must be greater than zero"
    }
    return ValidationResult(errors)
  }
}
