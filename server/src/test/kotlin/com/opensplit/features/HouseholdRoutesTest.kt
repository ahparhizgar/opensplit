package com.opensplit.features

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.HouseholdMemberResponse
import com.opensplit.dto.household.HouseholdOverviewResponse
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.dto.household.JoinHouseholdResponse
import com.opensplit.createAuthenticatedClient
import com.opensplit.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HouseholdScenarios {
    @Test
    fun overviewMarksCurrentUser() = testOpenSplit {
        client.post("/households") {
            setBody(CreateHouseholdRequest("My Home"))
        }.also { assertEquals(HttpStatusCode.Created, it.status) }

        val overview = client.get("/households/overview").body<HouseholdOverviewResponse>()
        val currentUser = overview.members.find { it.isCurrentUser }
        assertEquals(1, overview.members.size)
        assertTrue(currentUser != null, "Current user should be marked as isCurrentUser")
    }
    @Test
    fun createAndJoinHousehold() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Maple House"))
        }.also {
            assertEquals(HttpStatusCode.Created, it.status)
        }.body<CreateHouseholdResponse>()

        val join = client.post("/households/join") {
            setBody(JoinHouseholdRequest(created.inviteCode!!))
        }.also {
            assertEquals(HttpStatusCode.OK, it.status)
        }.body<JoinHouseholdResponse>()

        assertEquals(created.id, join.householdId)
        assertTrue(join.joined)
    }

    @Test
    fun joinReturnsClearErrorForInvalidInviteCode() = testOpenSplit {
        val response = client.post("/households/join") {
            setBody(JoinHouseholdRequest("not-a-real-code"))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        val error = response.body<ErrorResponse>()
        assertEquals("Invalid invite code or household id", error.errors["inviteCodeOrId"])
    }

    @Test
    fun leavingLastHouseholdReturnsSafeLandingState() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("My Home"))
        }.body<CreateHouseholdResponse>()

        val otherUser = client.post("/users") {
            setBody(SignUpRequest("leave-test@example.com", "password123"))
        }.body<AuthSessionState>()
        val otherClient = createAuthenticatedClient(otherUser.accessToken)

        otherClient.post("/households/join") {
            setBody(JoinHouseholdRequest(created.inviteCode!!))
        }.also { assertEquals(HttpStatusCode.OK, it.status) }

        val leaveResponse = otherClient.delete("/households/${created.id}/memberships/me")
        assertEquals(HttpStatusCode.OK, leaveResponse.status)
        val afterLeave = leaveResponse.body<HouseholdOverviewResponse>()
        assertEquals(null, afterLeave.activeHouseholdId)
        assertEquals(0, afterLeave.households.size)
    }

    @Test
    fun joinByHouseholdIdRequiresMembership() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Maple House"))
        }.body<CreateHouseholdResponse>()

        val otherUser = client.post("/users") {
            setBody(SignUpRequest("member-check@example.com", "password123"))
        }.body<AuthSessionState>()
        val otherClient = createAuthenticatedClient(otherUser.accessToken)

        val joinById = otherClient.post("/households/join") {
            setBody(JoinHouseholdRequest(created.id))
        }

        assertEquals(HttpStatusCode.Forbidden, joinById.status)
        val error = joinById.body<ErrorResponse>()
        assertEquals("Missing permission to access this household", error.errors["permission"])
    }

    @Test
    fun createJoinAndCheckActiveContextSmokeTest() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Maple House"))
        }.body<CreateHouseholdResponse>()

        val otherUser = client.post("/users") {
            setBody(SignUpRequest("join-flow@example.com", "password123"))
        }.body<AuthSessionState>()
        val otherClient = createAuthenticatedClient(otherUser.accessToken)

        val join = otherClient.post("/households/join") {
            setBody(JoinHouseholdRequest(created.inviteCode!!))
        }.also {
            assertEquals(HttpStatusCode.OK, it.status)
        }.body<JoinHouseholdResponse>()

        val contextResponse = otherClient.get("/household-context")
        assertEquals(HttpStatusCode.OK, contextResponse.status)
        assertEquals(created.id, join.householdId)
    }

    @Test
    fun overviewIncludesInviteCode() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Family Home"))
        }.body<CreateHouseholdResponse>()

        val overview = client.get("/households/overview")
            .body<HouseholdOverviewResponse>()

        assertEquals(1, overview.households.size)
        val household = overview.households.first()
        assertEquals(created.inviteCode, household.inviteCode)
    }

    @Test
    fun ownerLeavesWithOtherMembersTransfersOwnership() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Our Home"))
        }.body<CreateHouseholdResponse>()

        val otherUser = client.post("/users") {
            setBody(SignUpRequest("owner-transfer@example.com", "password123"))
        }.body<AuthSessionState>()
        val otherClient = createAuthenticatedClient(otherUser.accessToken)

        otherClient.post("/households/join") {
            setBody(JoinHouseholdRequest(created.inviteCode!!))
        }.also { assertEquals(HttpStatusCode.OK, it.status) }

        // Owner leaves, ownership should transfer
        client.delete("/households/${created.id}/memberships/me").also {
            assertEquals(HttpStatusCode.OK, it.status)
        }

        // Other user checks overview — they should now be the owner
        val overview = otherClient.get("/households/overview").body<HouseholdOverviewResponse>()
        val me = overview.members.find { it.isCurrentUser }
        assertTrue(me != null, "Other user should still be a member")
        assertTrue(me.isOwner, "Ownership should have been transferred to the remaining member")
    }

    @Test
    fun ownerLeavesAsLastMemberHouseholdBecomesOwnerless() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Solo Home"))
        }.body<CreateHouseholdResponse>()

        client.delete("/households/${created.id}/memberships/me").also {
            assertEquals(HttpStatusCode.OK, it.status)
        }

        // Verify safe landing
        val overview = client.get("/households/overview").body<HouseholdOverviewResponse>()
        assertEquals(null, overview.activeHouseholdId, "Should have no active household")
        assertEquals(0, overview.households.size, "Should have no households")
    }

    @Test
    fun householdOverviewSwitchAndLeaveFlow() = testOpenSplit {
        val firstHousehold = client.post("/households") {
            setBody(CreateHouseholdRequest("Maple House"))
        }.body<CreateHouseholdResponse>()

        val secondHousehold = client.post("/households") {
            setBody(CreateHouseholdRequest("River House"))
        }.body<CreateHouseholdResponse>()

        val overviewResponse = client.get("/households/overview")
        assertEquals(HttpStatusCode.OK, overviewResponse.status)
        val overview = overviewResponse.body<HouseholdOverviewResponse>()
        assertEquals(secondHousehold.id, overview.activeHouseholdId)
        assertEquals(2, overview.households.size)
        assertEquals(1, overview.members.size)

        val switchResponse = client.post("/households/context") {
            setBody(mapOf("householdId" to firstHousehold.id))
        }
        assertEquals(HttpStatusCode.OK, switchResponse.status)
        val switched = switchResponse.body<HouseholdOverviewResponse>()
        assertEquals(firstHousehold.id, switched.activeHouseholdId)
        assertEquals(2, switched.households.size)

        val leaveResponse = client.delete("/households/${secondHousehold.id}/memberships/me")
        assertEquals(HttpStatusCode.OK, leaveResponse.status)
        val afterLeave = leaveResponse.body<HouseholdOverviewResponse>()
        assertEquals(firstHousehold.id, afterLeave.activeHouseholdId)
        assertEquals(1, afterLeave.households.size)
    }
}
