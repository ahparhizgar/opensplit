package com.opensplit.validation.expense

import com.opensplit.validation.auth.ValidationResult

object ExpenseValidation {
  fun validateExpense(title: String, amount: Double): ValidationResult {
    val errors = mutableMapOf<String, String>()
    if (title.isBlank()) {
      errors["title"] = "Title is required"
    }
    if (amount <= 0) {
      errors["amount"] = "Amount must be greater than zero"
    }
    return ValidationResult(errors)
  }
}
