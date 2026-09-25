package com.opensplit.integration.sync

import com.opensplit.database.*
import com.opensplit.dto.sync.*
import com.opensplit.integration.expense.ExpenseParticipantRecord
import com.opensplit.integration.expense.ExpenseRecord
import com.opensplit.integration.expense.toDto
import kotlin.time.Clock
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class SyncRepositoryImpl(private val database: Database) : SyncRepository {
  override fun recordChange(
      entityType: SyncEntityType,
      entityId: String,
      operation: SyncOperation,
  ): Long =
      transaction(database) {
        val logId =
            ChangeLog.insert {
                  it[ChangeLog.entityType] = entityType
                  it[ChangeLog.entityId] = entityId
                  it[ChangeLog.operation] = operation
                  it[ChangeLog.timestamp] = Clock.System.now()
                }[ChangeLog.id]

        when (entityType) {
          SyncEntityType.EXPENSE ->
              Expenses.update({ Expenses.id eq entityId }) { it[version] = logId }
          SyncEntityType.MEMBERSHIP ->
              Memberships.update({ Memberships.id eq entityId }) { it[version] = logId }
        }
        logId
      }

  override fun getChanges(sinceVersion: Long, userId: String): SyncResponse =
      transaction(database) {
        val latestVersion =
            ChangeLog.selectAll()
                .orderBy(ChangeLog.id to SortOrder.DESC)
                .limit(1)
                .firstOrNull()
                ?.get(ChangeLog.id) ?: sinceVersion

        val userGroupIds =
            Memberships.selectAll()
                .where { Memberships.userId eq userId }
                .map { it[Memberships.groupId] }
                .toSet()

        val changedExpenses =
            Expenses.selectAll()
                .where {
                  (Expenses.version greater sinceVersion) and (Expenses.groupId inList userGroupIds)
                }
                .map { row ->
                  val expenseId = row[Expenses.id]
                  val participants =
                      ExpenseParticipants.selectAll()
                          .where { ExpenseParticipants.expenseId eq expenseId }
                          .map { it.toParticipantRecord() }
                  row.toExpenseRecord(participants).toDto()
                }

        val deletedExpenses =
            ChangeLog.selectAll()
                .where {
                  (ChangeLog.id greater sinceVersion) and
                      (ChangeLog.entityType eq SyncEntityType.EXPENSE) and
                      (ChangeLog.operation eq SyncOperation.DELETE)
                }
                .map { it[ChangeLog.entityId] }

        SyncResponse(
            latestVersion = latestVersion,
            changedEntities = ChangedEntitiesDto(expenses = changedExpenses),
            deletedEntities = DeletedEntitiesDto(expenses = deletedExpenses),
        )
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
          createdAt = get(Expenses.createdAt),
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
