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

        // Verify other user is still in the household
        val overview = otherClient.get("/households/overview").body<HouseholdOverviewResponse>()
        assertEquals(1, overview.households.size)
        assertTrue(overview.households.first().isOwner, "Ownership should have been transferred")
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
        assertEquals(0, overview.households.size, "Should have no households")
    }
}
