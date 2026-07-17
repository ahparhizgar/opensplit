package com.opensplit.fake

import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.dto.household.HouseholdOverviewDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.features.household.HouseholdApi
import com.opensplit.util.FakeService

class FakeHouseholdApi : HouseholdApi, FakeService {
  override var errorToThrow: Exception? = null
  var overview =
      HouseholdOverviewDto(
          households =
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
              ),
          members =
              listOf(
                  HouseholdMemberDto(userId = "user-1", email = "amir@example.com", isOwner = true)
              ),
      )

  override suspend fun createHousehold(name: String): HouseholdDto = fakeApiCall {
    overview =
        HouseholdOverviewDto(
            households =
                listOf(
                    HouseholdSummaryDto(
                        id = "household-1",
                        name = name,
                        memberCount = 1,
                        inviteCode = "invite-abc123",
                    )
                ),
            members =
                listOf(
                    HouseholdMemberDto(
                        userId = "user-1",
                        email = "amir@example.com",
                        isOwner = true,
                    )
                ),
        )
    HouseholdDto(
        id = "household-1",
        name = name,
        inviteLink = "https://opensplit.com/join/invite-abc123",
        members = overview.members,
    )
  }

  override suspend fun joinHousehold(inviteCode: String): HouseholdDto = fakeApiCall {
    overview =
        overview.copy(
            households =
                listOf(
                    HouseholdSummaryDto(
                        id = "household-2",
                        name = "Joined House",
                        memberCount = 2,
                        inviteCode = "invite-def456",
                    ),
                ),
            members =
                listOf(
                    HouseholdMemberDto(
                        userId = "user-1",
                        email = "amir@example.com",
                        isOwner = false,
                    )
                ),
        )
    HouseholdDto(
        id = "household-2",
        name = "Joined House",
        inviteLink = "https://opensplit.com/join/invite-def456",
        members = overview.members,
    )
  }

  override suspend fun loadOverview(): HouseholdOverviewDto = fakeApiCall { overview }

  override suspend fun leaveHousehold(householdId: String): HouseholdOverviewDto = fakeApiCall {
    val remaining = overview.households.filterNot { it.id == householdId }
    overview =
        overview.copy(
            households = remaining,
            members = if (remaining.isEmpty()) emptyList() else overview.members,
        )
    overview
  }

  override suspend fun getHousehold(id: String): HouseholdDto = fakeApiCall {
    HouseholdDto(
        id = id,
        name = "Household ${id.take(5)}",
        inviteLink = "https://opensplit.com/join/invite-abc123",
        members = overview.members,
    )
  }
}
