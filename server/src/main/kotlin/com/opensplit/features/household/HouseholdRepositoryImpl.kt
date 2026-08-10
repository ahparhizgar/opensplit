package com.opensplit.features.household

import com.opensplit.database.Households
import com.opensplit.database.Memberships
import com.opensplit.database.Users
import com.opensplit.features.sync.SyncRepository
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class HouseholdRepositoryImpl(
    private val database: Database,
    private val syncRepository: SyncRepository,
) : HouseholdRepository {
  override fun loadHouseholds(userId: String): List<HouseholdDetailRecord> =
      transaction(database) {
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

          val members =
              (Users innerJoin Memberships)
                  .select(Users.id, Users.name, Users.email, Memberships.balance)
                  .where { Memberships.householdId eq householdId }
                  .map { it.toHouseholdMember(it[Memberships.balance]) }

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
        val membershipId = Uuid.random().toString()
        Memberships.insert {
          it[Memberships.id] = membershipId
          it[Memberships.householdId] = targetHouseholdId
          it[Memberships.userId] = ownerId
          it[Memberships.balance] = 0.0
        }

        syncRepository.recordChange("HOUSEHOLD", targetHouseholdId, "INSERT")
        syncRepository.recordChange("MEMBERSHIP", membershipId, "INSERT")

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
        val membershipId = Uuid.random().toString()
        Memberships.insert {
          it[Memberships.id] = membershipId
          it[Memberships.householdId] = householdId
          it[Memberships.userId] = userId
          it[Memberships.balance] = 0.0
        }
        syncRepository.recordChange("MEMBERSHIP", membershipId, "INSERT")
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

        val members =
            (Users innerJoin Memberships)
                .select(Users.id, Users.name, Users.email, Memberships.balance)
                .where { Memberships.householdId eq householdId }
                .map { it.toHouseholdMember(it[Memberships.balance]) }

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

      val membershipsToDelete =
          Memberships.selectAll()
              .where { (Memberships.householdId eq householdId) and (Memberships.userId eq userId) }
              .map { it[Memberships.id] }

      membershipsToDelete.forEach { membershipId ->
        syncRepository.recordChange("MEMBERSHIP", membershipId, "DELETE")
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

  private fun ResultRow.toHouseholdMember(balance: Double = 0.0): HouseholdMemberRecord =
      HouseholdMemberRecord(
          userId = get(Users.id),
          name = get(Users.name),
          email = get(Users.email),
          balance = balance,
      )
}
