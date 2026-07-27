package com.opensplit.features.sync

import com.opensplit.database.*
import com.opensplit.dto.expense.*
import com.opensplit.dto.household.*
import com.opensplit.dto.sync.*
import com.opensplit.features.expense.ExpenseParticipantRecord
import com.opensplit.features.expense.ExpenseRecord
import com.opensplit.features.expense.toDto
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class SyncRepositoryImpl(private val database: Database) : SyncRepository {
  override fun recordChange(entityType: String, entityId: String, operation: String): Long =
      transaction(database) {
        val logId =
            ChangeLog.insert {
                  it[ChangeLog.entityType] = entityType
                  it[ChangeLog.entityId] = entityId
                  it[ChangeLog.operation] = operation
                  it[ChangeLog.timestamp] = System.currentTimeMillis()
                }[ChangeLog.id]

        when (entityType) {
          "HOUSEHOLD" -> Households.update({ Households.id eq entityId }) { it[version] = logId }
          "EXPENSE" -> Expenses.update({ Expenses.id eq entityId }) { it[version] = logId }
          "MEMBERSHIP" -> Memberships.update({ Memberships.id eq entityId }) { it[version] = logId }
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

        val userHouseholdIds =
            Memberships.selectAll()
                .where { Memberships.userId eq userId }
                .map { it[Memberships.householdId] }
                .toSet()

        val changedHouseholds =
            Households.selectAll()
                .where {
                  (Households.version greater sinceVersion) and
                      (Households.id inList userHouseholdIds)
                }
                .map { it.toHouseholdDto(userId) }

        val changedExpenses =
            Expenses.selectAll()
                .where {
                  (Expenses.version greater sinceVersion) and
                      (Expenses.householdId inList userHouseholdIds)
                }
                .map { row ->
                  val expenseId = row[Expenses.id]
                  val participants =
                      ExpenseParticipants.selectAll()
                          .where { ExpenseParticipants.expenseId eq expenseId }
                          .map { it.toParticipantRecord() }
                  row.toExpenseRecord(participants).toDto()
                }

        val deletedHouseholds =
            ChangeLog.selectAll()
                .where {
                  (ChangeLog.id greater sinceVersion) and
                      (ChangeLog.entityType eq "HOUSEHOLD") and
                      (ChangeLog.operation eq "DELETE")
                }
                .map { it[ChangeLog.entityId] }

        val deletedExpenses =
            ChangeLog.selectAll()
                .where {
                  (ChangeLog.id greater sinceVersion) and
                      (ChangeLog.entityType eq "EXPENSE") and
                      (ChangeLog.operation eq "DELETE")
                }
                .map { it[ChangeLog.entityId] }

        SyncResponse(
            latestVersion = latestVersion,
            changedEntities =
                ChangedEntitiesDto(households = changedHouseholds, expenses = changedExpenses),
            deletedEntities =
                DeletedEntitiesDto(households = deletedHouseholds, expenses = deletedExpenses),
        )
      }

  private fun ResultRow.toHouseholdDto(currentUserId: String): HouseholdDto {
    val householdId = get(Households.id)
    val ownerId = get(Households.ownerId)
    val memberIds =
        Memberships.selectAll()
            .where { Memberships.householdId eq householdId }
            .map { it[Memberships.userId] }

    val members =
        Users.selectAll()
            .where { Users.id inList memberIds }
            .map { row ->
              HouseholdMemberDto(
                  userId = row[Users.id],
                  name = row[Users.name],
                  email = row[Users.email],
                  isOwner = row[Users.id] == ownerId,
                  isCurrentUser = row[Users.id] == currentUserId,
                  balance = 0.0, // Should be calculated or loaded
                  balanceCurrency = "IRR",
              )
            }

    return HouseholdDto(
        id = householdId,
        name = get(Households.name),
        members = members,
        inviteLink = "https://opensplit.com/join/${get(Households.inviteCode).orEmpty()}",
        isOwner = ownerId == currentUserId,
        balance = 0.0, // Should be calculated
    )
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
          splitMethod = Json.decodeFromString(get(Expenses.splitMethod)),
      )

  private fun ResultRow.toParticipantRecord(): ExpenseParticipantRecord =
      ExpenseParticipantRecord(
          userId = get(ExpenseParticipants.userId),
          paidAmount = get(ExpenseParticipants.paidAmount),
          owedAmount = get(ExpenseParticipants.owedAmount),
      )
}
