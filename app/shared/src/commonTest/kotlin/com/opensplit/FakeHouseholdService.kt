package com.opensplit

import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.dto.household.HouseholdOverviewDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.features.household.HouseholdService

class FakeHouseholdService : HouseholdService {
  var createCalls = 0
  var joinCalls = 0
  var loadOverviewCalls = 0
  var leaveCalls = 0

  fun withSingleHousehold(): FakeHouseholdService {
    overview =
        HouseholdOverviewDto(
            households =
                listOf(
                    HouseholdSummaryDto(
                        id = "household-1",
                        name = "Solo House",
                        memberCount = 1,
                        inviteCode = "invite-abc123",
                    ),
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
    return this
  }

  private var overview =
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

  override suspend fun createHousehold(name: String): HouseholdDto {
    createCalls++
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
    return HouseholdDto(
        id = "household-1",
        name = name,
        inviteLink = "https://opensplit.com/join/invite-abc123",
        members = overview.members,
    )
  }

  override suspend fun joinHousehold(inviteCode: String): HouseholdDto {
    joinCalls++
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
    return HouseholdDto(
        id = "household-2",
        name = "Joined House",
        inviteLink = "https://opensplit.com/join/invite-def456",
        members = overview.members,
    )
  }

  override suspend fun loadOverview(): HouseholdOverviewDto {
    loadOverviewCalls++
    return overview
  }

  override suspend fun leaveHousehold(householdId: String): HouseholdOverviewDto {
    leaveCalls++
    val remaining = overview.households.filterNot { it.id == householdId }
    overview =
        overview.copy(
            households = remaining,
            members = if (remaining.isEmpty()) emptyList() else overview.members,
        )
    return overview
  }

  override suspend fun getHousehold(id: String): HouseholdDto {
    return HouseholdDto(
        id = id,
        name = "Household ${id.take(5)}",
        inviteLink = "https://opensplit.com/join/invite-abc123",
        members = overview.members,
    )
  }
}
