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
        FakeHouseholdDtoFactory.create(
            id = "household-3",
            name = "Amir's House",
        )
    households = listOf(newHousehold)
    newHousehold
  }

  override suspend fun joinHousehold(inviteCode: String): HouseholdDto = fakeApiCall {
    val joinedHousehold =
        FakeHouseholdDtoFactory.create(
            id = "household-4",
            name = "Joined House",
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
                        name = "Amir",
                        email = "owner@example.com",
                        isOwner = true,
                        isCurrentUser = true,
                    ),
                    HouseholdMemberDto(
                        userId = "user-2",
                        name = "Other",
                        email = email,
                        isOwner = false,
                    ),
                ),
        )
      }

  override suspend fun getHousehold(id: String): HouseholdDto {
    return households.firstOrNull { it.id == id }
        ?: throw IllegalArgumentException("Household with id $id not found")
  }

  override suspend fun getHouseholds(): List<HouseholdDto> {
    return households
  }

  override suspend fun leaveHousehold(householdId: String): List<HouseholdDto> = fakeApiCall {
    households = households.filterNot { it.id == householdId }
    households
  }
}
