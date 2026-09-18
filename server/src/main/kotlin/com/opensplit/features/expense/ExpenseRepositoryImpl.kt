package com.opensplit.features.expense

import com.opensplit.database.ExpenseParticipants
import com.opensplit.database.Expenses
import com.opensplit.database.Memberships
import com.opensplit.features.sync.SyncRepository
import java.util.*
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ExpenseRepositoryImpl(
    private val database: Database,
    private val syncRepository: SyncRepository,
) : ExpenseRepository {
  override fun createExpense(expense: ExpenseRecord) {
    transaction(database) {
      Expenses.insert {
        it[id] = expense.id
        it[groupId] = expense.groupId
        it[title] = expense.title
        it[amount] = expense.amount
        it[creator] = expense.creator
        it[createdAt] = expense.createdAt.toEpochMilliseconds()
        it[splitMethod] = Json.encodeToString(expense.splitMethod)
      }

      expense.participants.forEach { participant ->
        ExpenseParticipants.insert {
          it[id] = UUID.randomUUID().toString()
          it[expenseId] = expense.id
          it[userId] = participant.userId
          it[paidAmount] = participant.paidAmount
          it[owedAmount] = participant.owedAmount
        }

        // Update Denormalized Balance
        Memberships.update({
          (Memberships.groupId eq expense.groupId) and (Memberships.userId eq participant.userId)
        }) {
          it[balance] = balance + (participant.paidAmount - participant.owedAmount)
        }
      }

      syncRepository.recordChange("EXPENSE", expense.id, "INSERT")
    }
  }

  override fun findExpensesByGroupId(groupId: String): List<ExpenseRecord> =
      transaction(database) {
        val expenseRows = Expenses.selectAll().where { Expenses.groupId eq groupId }.toList()

        expenseRows.map { row ->
          val expenseId = row[Expenses.id]
          val participants =
              ExpenseParticipants.selectAll()
                  .where { ExpenseParticipants.expenseId eq expenseId }
                  .map { it.toParticipantRecord() }

          row.toExpenseRecord(participants)
        }
      }

  override fun deleteExpense(expenseId: String) {
    transaction(database) {
      val expense =
          Expenses.selectAll().where { Expenses.id eq expenseId }.firstOrNull()
              ?: return@transaction
      val groupId = expense[Expenses.groupId]

      val participants =
          ExpenseParticipants.selectAll()
              .where { ExpenseParticipants.expenseId eq expenseId }
              .map { it.toParticipantRecord() }

      participants.forEach { participant ->
        // Reverse Denormalized Balance
        Memberships.update({
          (Memberships.groupId eq groupId) and (Memberships.userId eq participant.userId)
        }) {
          it[balance] = balance - (participant.paidAmount - participant.owedAmount)
        }
      }

      syncRepository.recordChange("EXPENSE", expenseId, "DELETE")
      ExpenseParticipants.deleteWhere { ExpenseParticipants.expenseId eq expenseId }
      Expenses.deleteWhere { Expenses.id eq expenseId }
    }
  }

  override fun updateExpense(expense: ExpenseRecord) {
    transaction(database) {
      // Get old expense to reverse balances
      val oldExpense =
          Expenses.selectAll().where { Expenses.id eq expense.id }.firstOrNull()
              ?: throw IllegalArgumentException("Expense not found")
      val groupId = oldExpense[Expenses.groupId]

      val oldParticipants =
          ExpenseParticipants.selectAll()
              .where { ExpenseParticipants.expenseId eq expense.id }
              .map { it.toParticipantRecord() }

      // Reverse old balances
      oldParticipants.forEach { participant ->
        Memberships.update({
          (Memberships.groupId eq groupId) and (Memberships.userId eq participant.userId)
        }) {
          it[balance] = balance - (participant.paidAmount - participant.owedAmount)
        }
      }

      // Update expense
      Expenses.update({ Expenses.id eq expense.id }) {
        it[title] = expense.title
        it[amount] = expense.amount
        it[creator] = expense.creator
        it[splitMethod] = Json.encodeToString(expense.splitMethod)
      }

      // Delete and recreate participants
      ExpenseParticipants.deleteWhere { ExpenseParticipants.expenseId eq expense.id }
      expense.participants.forEach { participant ->
        ExpenseParticipants.insert {
          it[id] = UUID.randomUUID().toString()
          it[expenseId] = expense.id
          it[userId] = participant.userId
          it[paidAmount] = participant.paidAmount
          it[owedAmount] = participant.owedAmount
        }

        // Apply new balances
        Memberships.update({
          (Memberships.groupId eq groupId) and (Memberships.userId eq participant.userId)
        }) {
          it[balance] = balance + (participant.paidAmount - participant.owedAmount)
        }
      }

      syncRepository.recordChange("EXPENSE", expense.id, "UPDATE")
    }
  }

  override fun findExpenseById(expenseId: String): ExpenseRecord? =
      transaction(database) {
        val expenseRow = Expenses.selectAll().where { Expenses.id eq expenseId }.firstOrNull()
        expenseRow?.let { row ->
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
          groupId = get(Expenses.groupId),
          title = get(Expenses.title),
          amount = get(Expenses.amount),
          creator = get(Expenses.creator),
          createdAt = Instant.fromEpochMilliseconds(get(Expenses.createdAt)),
          participants = participants,
          splitMethod = Json.decodeFromString(get(Expenses.splitMethod)),
      )

  private fun ResultRow.toParticipantRecord(): ExpenseParticipantRecord =
      ExpenseParticipantRecord(
          userId = get(ExpenseParticipants.userId),
          paidAmount = get(ExpenseParticipants.paidAmount),
          owedAmount = get(ExpenseParticipants.owedAmount),
      )
}
