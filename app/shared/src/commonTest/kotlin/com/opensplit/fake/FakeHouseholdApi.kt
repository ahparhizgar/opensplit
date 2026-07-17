package com.opensplit.fake

import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.features.household.HouseholdApi
import com.opensplit.util.FakeService

class FakeHouseholdApi : HouseholdApi, FakeService {
  override var errorToThrow: Exception? = null
  var households =
      listOf(
          HouseholdSummaryDto(
              id = "household-1",
              name = "Maple House",
              memberCount = 1,
              inviteCode = "invite-abc123",
          ),
          HouseholdSummaryDto(
              id = "household-2",
              name = "River House",
              memberCount = 2,
              inviteCode = "invite-def456",
          ),
      )

  override suspend fun createHousehold(name: String): HouseholdDto = fakeApiCall {
    households =
        listOf(
            HouseholdSummaryDto(
                id = "household-1",
                name = name,
                memberCount = 1,
                inviteCode = "invite-abc123",
            )
        )
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
  }

  override suspend fun joinHousehold(inviteCode: String): HouseholdDto = fakeApiCall {
    households =
        listOf(
            HouseholdSummaryDto(
                id = "household-2",
                name = "Joined House",
                memberCount = 2,
                inviteCode = "invite-def456",
            ),
        )
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
  }

  override suspend fun loadOverview(): List<HouseholdSummaryDto> = fakeApiCall { households }

  override suspend fun leaveHousehold(householdId: String): List<HouseholdSummaryDto> =
      fakeApiCall {
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
