package com.opensplit.fake

import com.opensplit.dto.group.FakeGroupDtoFactory
import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.GroupMemberDto
import com.opensplit.features.group.GroupApi
import com.opensplit.util.FakeService

class FakeGroupApi : GroupApi, FakeService {
  override var errorToThrow: Exception? = null
  var groups =
      listOf(
          FakeGroupDtoFactory.create(
              id = "group-1",
              name = "Maple House",
          ),
          FakeGroupDtoFactory.create(
              id = "group-2",
              name = "River House",
          ),
      )

  override suspend fun createGroup(name: String): GroupDto = fakeApiCall {
    val newGroup =
        FakeGroupDtoFactory.create(
            id = "group-3",
            name = "Amir's House",
        )
    groups = listOf(newGroup)
    newGroup
  }

  override suspend fun joinGroup(inviteCode: String): GroupDto = fakeApiCall {
    val joinedGroup =
        FakeGroupDtoFactory.create(
            id = "group-4",
            name = "Joined House",
        )
    groups = listOf(joinedGroup)
    joinedGroup
  }

  override suspend fun addMemberByEmail(groupId: String, email: String): GroupDto = fakeApiCall {
    GroupDto(
        id = groupId,
        name = "Maple House",
        inviteLink = "https://opensplit.com/join/invite-abc123",
        members =
            listOf(
                GroupMemberDto(
                    userId = "user-1",
                    name = "Amir",
                    email = "owner@example.com",
                    isOwner = true,
                    isCurrentUser = true,
                ),
                GroupMemberDto(
                    userId = "user-2",
                    name = "Other",
                    email = email,
                    isOwner = false,
                ),
            ),
    )
  }

  override suspend fun getGroup(id: String): GroupDto {
    return groups.firstOrNull { it.id == id }
        ?: throw IllegalArgumentException("Group with id $id not found")
  }

  override suspend fun getGroups(): List<GroupDto> {
    return groups
  }

  override suspend fun leaveGroup(groupId: String): List<GroupDto> = fakeApiCall {
    groups = groups.filterNot { it.id == groupId }
    groups
  }
}
