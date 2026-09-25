package com.opensplit.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.opensplit.db.AppDatabase
import com.opensplit.db.GroupDao
import com.opensplit.db.toDomain
import com.opensplit.db.toEntity
import com.opensplit.domain.Group
import com.opensplit.dto.group.GroupDto
import com.opensplit.integration.group.GroupApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class GroupRepository(
    private val api: GroupApi,
    private val dao: GroupDao,
    private val database: AppDatabase,
    private val scope: CoroutineScope,
) {
  fun getGroups(): Flow<List<Group>> {
    return dao.getGroupsWithMembers().map { entities -> entities.map { it.toDomain() } }
  }

  fun observeGroup(id: String): Flow<Group?> {
    return dao.observeGroupWithMembers(id).map { it?.toDomain() }
  }

  fun refresh() = scope.launch {
    try {
      val result = api.getGroups()
      database.useWriterConnection { connection ->
        connection.immediateTransaction {
          result.forEach { dto -> saveGroupWithPendingAdjustment(dto) }
        }
      }
    } catch (_: Exception) {
      // Ignore errors for background refresh in offline-first
    }
  }

  suspend fun getGroup(id: String): Group? {
    return dao.getGroupWithMembers(id)?.toDomain()
  }

  /**
   * Saves group from server but preserves local balance calculation. Local transactions are the
   * source of truth for balances.
   */
  private suspend fun saveGroupWithPendingAdjustment(dto: GroupDto) {
    val existingMembers =
        dao.getGroupWithMembers(dto.id)?.members?.associateBy { it.userId } ?: emptyMap()

    val adjustedMemberBalances =
        dto.members.associate { m -> m.userId to (existingMembers[m.userId]?.balance ?: 0.0) }

    val groupEntity = dto.toEntity()
    val memberEntities =
        dto.members.map { m ->
          m.toEntity(dto.id).copy(balance = adjustedMemberBalances[m.userId] ?: m.balance)
        }

    dao.insertGroupWithMembers(groupEntity, memberEntities)
  }

  suspend fun leaveGroup(groupId: String) {
    api.leaveGroup(groupId)
    database.useWriterConnection { connection ->
      connection.immediateTransaction { dao.deleteGroup(groupId) }
    }
  }

  suspend fun createGroup(name: String): Group {
    val result = api.createGroup(name)
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        dao.insertGroupWithMembers(
            result.toEntity(),
            result.members.map { it.toEntity(result.id) },
        )
      }
    }
    return result.toDomain()
  }

  suspend fun joinGroup(inviteCode: String): Group {
    val result = api.joinGroup(inviteCode)
    database.useWriterConnection { connection ->
      connection.immediateTransaction {
        dao.insertGroupWithMembers(
            result.toEntity(),
            result.members.map { it.toEntity(result.id) },
        )
      }
    }
    return result.toDomain()
  }
}
