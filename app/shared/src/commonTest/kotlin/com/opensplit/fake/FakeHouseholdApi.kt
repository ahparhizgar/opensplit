package com.opensplit.fake

import com.opensplit.dto.household.FakeHouseholdDtoFactory
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.features.household.HouseholdApi
import com.opensplit.util.FakeService

class FakeHouseholdApi : HouseholdApi, FakeService {
  override var errorToThrow: Exception? = null
  var households =
      listOf(
          FakeHouseholdDtoFactory.create(
              id = "household-1",
              name = "Maple House",
          ),
          FakeHouseholdDtoFactory.create(
              id = "household-2",
              name = "River House",
          ),
      )

  override suspend fun createHousehold(name: String): HouseholdDto = fakeApiCall {
    val newHousehold =
        HouseholdDto(
            id = "household-1",
            name = name,
            inviteLink = "https://opensplit.com/join/invite-abc123",
            members =
                listOf(
                    HouseholdMemberDto(
                        userId = "user-1",
                        email = "amir@example.com",
                        isOwner = true,
                    )
                ),
        )
    households = listOf(newHousehold)
    newHousehold
  }

  override suspend fun joinHousehold(inviteCode: String): HouseholdDto = fakeApiCall {
    val joinedHousehold =
        HouseholdDto(
            id = "household-2",
            name = "Joined House",
            inviteLink = "https://opensplit.com/join/invite-def456",
            members =
                listOf(
                    HouseholdMemberDto(
                        userId = "user-1",
                        email = "amir@example.com",
                        isOwner = false,
                    )
                ),
        )
    households = listOf(joinedHousehold)
    joinedHousehold
  }

  override suspend fun addMemberByEmail(householdId: String, email: String): HouseholdDto =
      fakeApiCall {
        HouseholdDto(
            id = householdId,
            name = "Maple House",
            inviteLink = "https://opensplit.com/join/invite-abc123",
            members =
                listOf(
                    HouseholdMemberDto(
                        userId = "user-1",
                        email = "owner@example.com",
                        isOwner = true,
                    ),
                    HouseholdMemberDto(
                        userId = "user-2",
                        email = email,
                        isOwner = false,
                    ),
                ),
        )
      }

  override suspend fun loadOverview(): List<HouseholdDto> = fakeApiCall { households }

  override suspend fun leaveHousehold(householdId: String): List<HouseholdDto> = fakeApiCall {
    households = households.filterNot { it.id == householdId }
    households
  }

  override suspend fun getHousehold(id: String): HouseholdDto = fakeApiCall {
    HouseholdDto(
        id = id,
        name = "Household ${id.take(5)}",
        inviteLink = "https://opensplit.com/join/invite-abc123",
        members =
            listOf(
                HouseholdMemberDto(
                    userId = "user-1",
                    email = "amir@example.com",
                    isOwner = true,
                )
            ),
    )
  }
}
