package com.opensplit.features

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.CreateHouseholdResponse
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
        assertEquals(firstHousehold.id, overview.activeHouseholdId)
        assertEquals(2, overview.households.size)
        assertEquals(1, overview.members.size)

        val switchResponse = client.post("/households/context") {
            setBody(mapOf("householdId" to secondHousehold.id))
        }
        assertEquals(HttpStatusCode.OK, switchResponse.status)
        val switched = switchResponse.body<HouseholdOverviewResponse>()
        assertEquals(secondHousehold.id, switched.activeHouseholdId)
        assertEquals(2, switched.households.size)

        val leaveResponse = client.delete("/households/${secondHousehold.id}/memberships/me")
        assertEquals(HttpStatusCode.OK, leaveResponse.status)
        val afterLeave = leaveResponse.body<HouseholdOverviewResponse>()
        assertEquals(firstHousehold.id, afterLeave.activeHouseholdId)
        assertEquals(1, afterLeave.households.size)
    }
}
