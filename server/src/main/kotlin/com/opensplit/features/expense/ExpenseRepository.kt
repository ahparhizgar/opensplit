package com.opensplit.features.expense

interface ExpenseRepository {
  fun createExpense(expense: ExpenseRecord)

  fun findExpensesByHouseholdId(householdId: String): List<ExpenseRecord>

  fun deleteExpense(expenseId: String)

  fun updateExpense(expense: ExpenseRecord)

  fun findExpenseById(expenseId: String): ExpenseRecord?
}
