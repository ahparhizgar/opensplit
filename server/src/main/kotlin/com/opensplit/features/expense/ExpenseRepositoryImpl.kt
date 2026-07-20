package com.opensplit.features.expense

import com.opensplit.database.ExpenseParticipants
import com.opensplit.database.Expenses
import java.util.*
import kotlin.time.Instant
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
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

      expense.participants.forEach { participant ->
        ExpenseParticipants.insert {
          it[id] = UUID.randomUUID().toString()
          it[expenseId] = expense.id
          it[userId] = participant.userId
          it[paidAmount] = participant.paidAmount
          it[owedAmount] = participant.owedAmount
        }
      }
    }
  }

  override fun findExpensesByHouseholdId(householdId: String): List<ExpenseRecord> =
      transaction(database) {
        val expenseRows =
            Expenses.selectAll().where { Expenses.householdId eq householdId }.toList()

        expenseRows.map { row ->
          val expenseId = row[Expenses.id]
          val participants =
              ExpenseParticipants.selectAll()
                  .where { ExpenseParticipants.expenseId eq expenseId }
                  .map { it.toParticipantRecord() }

          row.toExpenseRecord(participants)
        }
      }

  private fun ResultRow.toExpenseRecord(
      participants: List<ExpenseParticipantRecord>
  ): ExpenseRecord =
      ExpenseRecord(
          id = get(Expenses.id),
          householdId = get(Expenses.householdId),
          title = get(Expenses.title),
          amount = get(Expenses.amount),
          payerId = get(Expenses.payerId),
          createdAt = Instant.fromEpochMilliseconds(get(Expenses.createdAt)),
          participants = participants,
      )

  private fun ResultRow.toParticipantRecord(): ExpenseParticipantRecord =
      ExpenseParticipantRecord(
          userId = get(ExpenseParticipants.userId),
          paidAmount = get(ExpenseParticipants.paidAmount),
          owedAmount = get(ExpenseParticipants.owedAmount),
      )
}
