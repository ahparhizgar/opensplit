package com.opensplit

import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.HouseholdMemberResponse
import com.opensplit.dto.household.HouseholdOverviewResponse
import com.opensplit.dto.household.HouseholdSummaryResponse
import com.opensplit.dto.household.JoinHouseholdResponse
import com.opensplit.features.household.HouseholdService

class FakeHouseholdService : HouseholdService {
    var createCalls = 0
    var joinCalls = 0
    var loadOverviewCalls = 0
    var leaveCalls = 0

    fun withSingleHousehold(): FakeHouseholdService {
        overview = HouseholdOverviewResponse(
            households = listOf(
                HouseholdSummaryResponse(id = "household-1", name = "Solo House", memberCount = 1, inviteCode = "invite-abc123"),
            ),
            members = listOf(HouseholdMemberResponse(userId = "user-1", email = "amir@example.com", isOwner = true)),
        )
        return this
    }

    private var overview = HouseholdOverviewResponse(
        households = listOf(
            HouseholdSummaryResponse(id = "household-1", name = "Maple House", memberCount = 1, inviteCode = "invite-abc123"),
            HouseholdSummaryResponse(id = "household-2", name = "River House", memberCount = 2, inviteCode = "invite-def456"),
        ),
        members = listOf(HouseholdMemberResponse(userId = "user-1", email = "amir@example.com", isOwner = true)),
    )

    override suspend fun createHousehold(name: String): CreateHouseholdResponse {
        createCalls++
        overview = HouseholdOverviewResponse(
            households = listOf(HouseholdSummaryResponse(id = "household-1", name = name, memberCount = 1, inviteCode = "invite-abc123")),
            members = listOf(HouseholdMemberResponse(userId = "user-1", email = "amir@example.com", isOwner = true)),
        )
        return CreateHouseholdResponse(
            id = "household-1",
            name = name,
            inviteCode = "invite-abc123",
        )
    }

    override suspend fun joinHousehold(inviteCode: String): JoinHouseholdResponse {
        joinCalls++
        overview = overview.copy(
            households = listOf(
                HouseholdSummaryResponse(id = "household-2", name = "Joined House", memberCount = 2, inviteCode = "invite-def456"),
            ),
            members = listOf(HouseholdMemberResponse(userId = "user-1", email = "amir@example.com", isOwner = false)),
        )
        return JoinHouseholdResponse(
            householdId = "household-2",
            joined = true,
        )
    }

    override suspend fun loadOverview(): HouseholdOverviewResponse {
        loadOverviewCalls++
        return overview
    }

    override suspend fun leaveHousehold(householdId: String): HouseholdOverviewResponse {
        leaveCalls++
        val remaining = overview.households.filterNot { it.id == householdId }
        overview = overview.copy(
            households = remaining,
            members = if (remaining.isEmpty()) emptyList() else overview.members,
        )
        return overview
    }
}
