package com.opensplit

import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.JoinHouseholdResponse
import com.opensplit.features.household.HouseholdGateway

class FakeHouseholdGateway : HouseholdGateway {
    var createCalls = 0
    var joinCalls = 0

    override suspend fun createHousehold(name: String): CreateHouseholdResponse {
        createCalls++
        return CreateHouseholdResponse(
            id = "household-1",
            name = name,
            inviteCode = "invite-abc123",
        )
    }

    override suspend fun joinHousehold(inviteCode: String): JoinHouseholdResponse {
        joinCalls++
        return JoinHouseholdResponse(
            householdId = "household-2",
            joined = true,
        )
    }
}
