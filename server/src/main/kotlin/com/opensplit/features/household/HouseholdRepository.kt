package com.opensplit.features.household

interface HouseholdRepository {
  fun loadHouseholds(userId: String): List<HouseholdDetailRecord>

  fun createHousehold(name: String, ownerId: String): HouseholdRecord

  fun findHouseholdByInviteCode(inviteCode: String): HouseholdRecord?

  fun findHouseholdById(householdId: String): HouseholdRecord?

  fun hasMembership(householdId: String, userId: String): Boolean

  fun ensureMembership(householdId: String, userId: String)

  fun findMemberByEmail(email: String): HouseholdMemberRecord?

  fun loadHouseholdDetail(householdId: String, currentUserId: String): HouseholdDetailRecord?

  fun leaveHousehold(householdId: String, userId: String)
}
