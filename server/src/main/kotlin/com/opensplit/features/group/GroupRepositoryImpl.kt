package com.opensplit.features.group

import com.opensplit.database.Groups
import com.opensplit.database.Memberships
import com.opensplit.database.Users
import com.opensplit.features.sync.SyncRepository
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class GroupRepositoryImpl(
    private val database: Database,
    private val syncRepository: SyncRepository,
) : GroupRepository {
  override fun loadGroups(userId: String): List<GroupDetailRecord> =
      transaction(database) {
        val groupIds =
            Memberships.selectAll()
                .where { Memberships.userId eq userId }
                .map { it[Memberships.groupId] }

        groupIds.mapNotNull { groupId ->
          val group =
              Groups.selectAll()
                  .where { Groups.id eq groupId }
                  .limit(1)
                  .firstOrNull()
                  ?.toGroupRecord() ?: return@mapNotNull null

          val members =
              (Users innerJoin Memberships)
                  .select(Users.id, Users.name, Users.email, Memberships.balance)
                  .where { Memberships.groupId eq groupId }
                  .map { it.toGroupMember(it[Memberships.balance]) }

          GroupDetailRecord(group = group, members = members)
        }
      }

  override fun createGroup(name: String, ownerId: String): GroupRecord =
      transaction(database) {
        val targetGroupId = Uuid.random().toString()
        val inviteCode = Uuid.random().toString().replace("-", "").take(12)

        Groups.insert {
          it[Groups.id] = targetGroupId
          it[Groups.name] = name
          it[Groups.ownerId] = ownerId
          it[Groups.inviteCode] = inviteCode
        }
        val membershipId = Uuid.random().toString()
        Memberships.insert {
          it[Memberships.id] = membershipId
          it[Memberships.groupId] = targetGroupId
          it[Memberships.userId] = ownerId
          it[Memberships.balance] = 0.0
        }

        syncRepository.recordChange("HOUSEHOLD", targetGroupId, "INSERT")
        syncRepository.recordChange("MEMBERSHIP", membershipId, "INSERT")

        GroupRecord(
            id = targetGroupId,
            name = name,
            ownerId = ownerId,
            inviteCode = inviteCode,
        )
      }

  override fun findGroupByInviteCode(inviteCode: String): GroupRecord? =
      transaction(database) {
        Groups.selectAll()
            .where { Groups.inviteCode eq inviteCode }
            .limit(1)
            .firstOrNull()
            ?.toGroupRecord()
      }

  override fun findGroupById(groupId: String): GroupRecord? =
      transaction(database) {
        Groups.selectAll().where { Groups.id eq groupId }.limit(1).firstOrNull()?.toGroupRecord()
      }

  override fun hasMembership(groupId: String, userId: String): Boolean =
      transaction(database) {
        Memberships.selectAll()
            .where { (Memberships.groupId eq groupId) and (Memberships.userId eq userId) }
            .any()
      }

  override fun ensureMembership(groupId: String, userId: String) {
    transaction(database) {
      val alreadyMember =
          Memberships.selectAll()
              .where { (Memberships.groupId eq groupId) and (Memberships.userId eq userId) }
              .any()
      if (!alreadyMember) {
        val membershipId = Uuid.random().toString()
        Memberships.insert {
          it[Memberships.id] = membershipId
          it[Memberships.groupId] = groupId
          it[Memberships.userId] = userId
          it[Memberships.balance] = 0.0
        }
        syncRepository.recordChange("MEMBERSHIP", membershipId, "INSERT")
      }
    }
  }

  override fun findMemberByEmail(email: String): GroupMemberRecord? =
      transaction(database) {
        Users.selectAll().where { Users.email eq email }.limit(1).firstOrNull()?.toGroupMember()
      }

  override fun loadGroupDetail(
      groupId: String,
      currentUserId: String,
  ): GroupDetailRecord? =
      transaction(database) {
        val isMember =
            Memberships.selectAll()
                .where {
                  (Memberships.groupId eq groupId) and (Memberships.userId eq currentUserId)
                }
                .any()
        if (!isMember) {
          return@transaction null
        }

        val group =
            Groups.selectAll()
                .where { Groups.id eq groupId }
                .limit(1)
                .firstOrNull()
                ?.toGroupRecord() ?: return@transaction null

        val members =
            (Users innerJoin Memberships)
                .select(Users.id, Users.name, Users.email, Memberships.balance)
                .where { Memberships.groupId eq groupId }
                .map { it.toGroupMember(it[Memberships.balance]) }

        GroupDetailRecord(group = group, members = members)
      }

  override fun leaveGroup(groupId: String, userId: String) {
    transaction(database) {
      val group = Groups.selectAll().where { Groups.id eq groupId }.limit(1).firstOrNull()
      if (group != null && group[Groups.ownerId] == userId) {
        val nextOwner =
            Memberships.selectAll()
                .where { (Memberships.groupId eq groupId) and (Memberships.userId neq userId) }
                .limit(1)
                .firstOrNull()
        if (nextOwner != null) {
          Groups.update({ Groups.id eq groupId }) {
            it[Groups.ownerId] = nextOwner[Memberships.userId]
          }
        }
      }

      val membershipsToDelete =
          Memberships.selectAll()
              .where { (Memberships.groupId eq groupId) and (Memberships.userId eq userId) }
              .map { it[Memberships.id] }

      membershipsToDelete.forEach { membershipId ->
        syncRepository.recordChange("MEMBERSHIP", membershipId, "DELETE")
      }

      Memberships.deleteWhere {
        (Memberships.groupId eq groupId) and (Memberships.userId eq userId)
      }
    }
  }

  private fun ResultRow.toGroupRecord(): GroupRecord =
      GroupRecord(
          id = get(Groups.id),
          name = get(Groups.name),
          ownerId = get(Groups.ownerId),
          inviteCode = get(Groups.inviteCode),
      )

  private fun ResultRow.toGroupMember(balance: Double = 0.0): GroupMemberRecord =
      GroupMemberRecord(
          userId = get(Users.id),
          name = get(Users.name),
          email = get(Users.email),
          balance = balance,
      )
}
