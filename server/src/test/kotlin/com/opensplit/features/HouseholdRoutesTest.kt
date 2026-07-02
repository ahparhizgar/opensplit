package com.opensplit.features

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.NewHouseholdDto
import com.opensplit.dto.household.HouseholdOverviewDto
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.createAuthenticatedClient
import com.opensplit.createOtherClient
import com.opensplit.dto.household.HouseholdDto
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
        }.body<NewHouseholdDto>()

        client.post("/households/join") {
            setBody(JoinHouseholdRequest(created.inviteCode!!))
        }.also {
            assertEquals(HttpStatusCode.OK, it.status)
        }
    }

    @Test
    fun getHousehold_notFound() = testOpenSplit {
        val r = client.get("/households/123456789")
        assertEquals(HttpStatusCode.NotFound, r.status)
    }

    @Test
    fun getNotMineHousehold_notFound() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Maple House"))
        }.body<NewHouseholdDto>()

        val otherClient = createOtherClient()

        val r = otherClient.get("/households/${created.id}")
        assertEquals(HttpStatusCode.NotFound, r.status)
    }

    @Test
    fun getHousehold_returns() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Maple House"))
        }.body<NewHouseholdDto>()

        val r = client.get("/households/${created.id}")
        assertEquals(HttpStatusCode.OK, r.status)
        val household = r.body<HouseholdDto>()
        assertEquals(created.id, household.id)
        assertEquals(created.name, household.name)
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
        }.body<NewHouseholdDto>()

        val otherUser = client.post("/users") {
            setBody(SignUpRequest("leave-test@example.com", "password123"))
        }.body<AuthSessionState>()
        val otherClient = createAuthenticatedClient(otherUser.accessToken)

        otherClient.post("/households/join") {
            setBody(JoinHouseholdRequest(created.inviteCode!!))
        }.also { assertEquals(HttpStatusCode.OK, it.status) }

        val leaveResponse = otherClient.delete("/households/${created.id}/memberships/me")
        assertEquals(HttpStatusCode.OK, leaveResponse.status)
        val afterLeave = leaveResponse.body<HouseholdOverviewDto>()
        assertEquals(0, afterLeave.households.size)
    }

    @Test
    fun joinByHouseholdIdRequiresMembership() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Maple House"))
        }.body<NewHouseholdDto>()

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
        }.body<NewHouseholdDto>()

        val overview = client.get("/households/overview")
            .body<HouseholdOverviewDto>()

        assertEquals(1, overview.households.size)
        val household = overview.households.first()
        assertEquals(created.inviteCode, household.inviteCode)
    }

    @Test
    fun ownerLeavesWithOtherMembersTransfersOwnership() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Our Home"))
        }.body<NewHouseholdDto>()

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
        val overview = otherClient.get("/households/overview").body<HouseholdOverviewDto>()
        assertEquals(1, overview.households.size)
        assertTrue(overview.households.first().isOwner, "Ownership should have been transferred")
    }

    @Test
    fun ownerLeavesAsLastMemberHouseholdBecomesOwnerless() = testOpenSplit {
        val created = client.post("/households") {
            setBody(CreateHouseholdRequest("Solo Home"))
        }.body<NewHouseholdDto>()

        client.delete("/households/${created.id}/memberships/me").also {
            assertEquals(HttpStatusCode.OK, it.status)
        }

        // Verify safe landing
        val overview = client.get("/households/overview").body<HouseholdOverviewDto>()
        assertEquals(0, overview.households.size, "Should have no households")
    }
}
