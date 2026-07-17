package com.opensplit.features.household

import com.opensplit.database.Households
import com.opensplit.database.Memberships
import com.opensplit.database.Users
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class HouseholdRepositoryImpl(private val database: Database) : HouseholdRepository {
  override fun loadHouseholds(userId: String): List<HouseholdDetailRecord> =
      transaction(database) {
        // TODO fix N+1 query
        val householdIds =
            Memberships.selectAll()
                .where { Memberships.userId eq userId }
                .map { it[Memberships.householdId] }

        householdIds.mapNotNull { householdId ->
          val household =
              Households.selectAll()
                  .where { Households.id eq householdId }
                  .limit(1)
                  .firstOrNull()
                  ?.toHouseholdRecord() ?: return@mapNotNull null

          val memberIds =
              Memberships.selectAll()
                  .where { Memberships.householdId eq householdId }
                  .map { it[Memberships.userId] }
          val members =
              Users.selectAll().where { Users.id inList memberIds }.map { it.toHouseholdMember() }

          HouseholdDetailRecord(household = household, members = members)
        }
      }

  override fun createHousehold(name: String, ownerId: String): HouseholdRecord =
      transaction(database) {
        val targetHouseholdId = Uuid.random().toString()
        val inviteCode = Uuid.random().toString().replace("-", "").take(12)

        Households.insert {
          it[Households.id] = targetHouseholdId
          it[Households.name] = name
          it[Households.ownerId] = ownerId
          it[Households.inviteCode] = inviteCode
        }
        Memberships.insert {
          it[Memberships.id] = Uuid.random().toString()
          it[Memberships.householdId] = targetHouseholdId
          it[Memberships.userId] = ownerId
        }

        HouseholdRecord(
            id = targetHouseholdId,
            name = name,
            ownerId = ownerId,
            inviteCode = inviteCode,
        )
      }

  override fun findHouseholdByInviteCode(inviteCode: String): HouseholdRecord? =
      transaction(database) {
        Households.selectAll()
            .where { Households.inviteCode eq inviteCode }
            .limit(1)
            .firstOrNull()
            ?.toHouseholdRecord()
      }

  override fun findHouseholdById(householdId: String): HouseholdRecord? =
      transaction(database) {
        Households.selectAll()
            .where { Households.id eq householdId }
            .limit(1)
            .firstOrNull()
            ?.toHouseholdRecord()
      }

  override fun hasMembership(householdId: String, userId: String): Boolean =
      transaction(database) {
        Memberships.selectAll()
            .where { (Memberships.householdId eq householdId) and (Memberships.userId eq userId) }
            .any()
      }

  override fun ensureMembership(householdId: String, userId: String) {
    transaction(database) {
      val alreadyMember =
          Memberships.selectAll()
              .where { (Memberships.householdId eq householdId) and (Memberships.userId eq userId) }
              .any()
      if (!alreadyMember) {
        Memberships.insert {
          it[Memberships.id] = Uuid.random().toString()
          it[Memberships.householdId] = householdId
          it[Memberships.userId] = userId
        }
      }
    }
  }

  override fun findMemberByEmail(email: String): HouseholdMemberRecord? =
      transaction(database) {
        Users.selectAll().where { Users.email eq email }.limit(1).firstOrNull()?.toHouseholdMember()
      }

  override fun loadHouseholdDetail(
      householdId: String,
      currentUserId: String,
  ): HouseholdDetailRecord? =
      transaction(database) {
        val isMember =
            Memberships.selectAll()
                .where {
                  (Memberships.householdId eq householdId) and (Memberships.userId eq currentUserId)
                }
                .any()
        if (!isMember) {
          return@transaction null
        }

        val household =
            Households.selectAll()
                .where { Households.id eq householdId }
                .limit(1)
                .firstOrNull()
                ?.toHouseholdRecord() ?: return@transaction null

        val memberIds =
            Memberships.selectAll()
                .where { Memberships.householdId eq householdId }
                .map { it[Memberships.userId] }
        val members =
            Users.selectAll().where { Users.id inList memberIds }.map { it.toHouseholdMember() }

        HouseholdDetailRecord(household = household, members = members)
      }

  override fun leaveHousehold(householdId: String, userId: String) {
    transaction(database) {
      val household =
          Households.selectAll().where { Households.id eq householdId }.limit(1).firstOrNull()
      if (household != null && household[Households.ownerId] == userId) {
        val nextOwner =
            Memberships.selectAll()
                .where {
                  (Memberships.householdId eq householdId) and (Memberships.userId neq userId)
                }
                .limit(1)
                .firstOrNull()
        if (nextOwner != null) {
          Households.update({ Households.id eq householdId }) {
            it[Households.ownerId] = nextOwner[Memberships.userId]
          }
        }
      }

      Memberships.deleteWhere {
        (Memberships.householdId eq householdId) and (Memberships.userId eq userId)
      }
    }
  }

  private fun ResultRow.toHouseholdRecord(): HouseholdRecord =
      HouseholdRecord(
          id = get(Households.id),
          name = get(Households.name),
          ownerId = get(Households.ownerId),
          inviteCode = get(Households.inviteCode),
      )

  private fun ResultRow.toHouseholdMember(): HouseholdMemberRecord =
      HouseholdMemberRecord(
          userId = get(Users.id),
          name = get(Users.name),
          email = get(Users.email),
      )
}
