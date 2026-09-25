package com.opensplit.integration.expense

interface ExpenseRepository {
  fun createExpense(expense: ExpenseRecord)

  fun findExpensesByGroupId(groupId: String): List<ExpenseRecord>

  fun deleteExpense(expenseId: String)

  fun updateExpense(expense: ExpenseRecord)

  fun findExpenseById(expenseId: String): ExpenseRecord?
}
