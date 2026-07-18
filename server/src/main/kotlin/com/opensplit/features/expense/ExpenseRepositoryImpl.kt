package com.opensplit.features.expense

import com.opensplit.database.Expenses
import kotlin.time.Instant
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ExpenseRepositoryImpl(private val database: Database) : ExpenseRepository {
  override fun createExpense(expense: ExpenseRecord) {
    transaction(database) {
      Expenses.insert {
        it[id] = expense.id
        it[householdId] = expense.householdId
        it[title] = expense.title
        it[amount] = expense.amount
        it[payerId] = expense.payerId
        it[createdAt] = expense.createdAt.toEpochMilliseconds()
      }
    }
  }

  override fun findExpensesByHouseholdId(householdId: String): List<ExpenseRecord> =
      transaction(database) {
        Expenses.selectAll()
            .where { Expenses.householdId eq householdId }
            .map { it.toExpenseRecord() }
      }

  private fun ResultRow.toExpenseRecord(): ExpenseRecord =
      ExpenseRecord(
          id = get(Expenses.id),
          householdId = get(Expenses.householdId),
          title = get(Expenses.title),
          amount = get(Expenses.amount),
          payerId = get(Expenses.payerId),
          createdAt = Instant.fromEpochMilliseconds(get(Expenses.createdAt)),
      )
}
