package com.opensplit.features.expense

interface ExpenseRepository {
  fun createExpense(expense: ExpenseRecord)

  fun findExpensesByHouseholdId(householdId: String): List<ExpenseRecord>
}
