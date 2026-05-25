package com.opensplit.features

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.dto.household.JoinHouseholdResponse
import com.opensplit.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
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

    private fun ApplicationTestBuilder.createAuthenticatedClient(accessToken: String) = createClient {
        install(ContentNegotiation) {
            json()
        }
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
        }
        install(Auth) {
            bearer {
                cacheTokens = false
                loadTokens {
                    BearerTokens(accessToken = accessToken, refreshToken = null)
                }
            }
        }
    }
}
