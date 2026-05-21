package com.opensplit.features

import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HouseholdRoutesTest {
    @Test
    fun createAndJoinHouseholdFlow() = testOpenSplit {
        val signUp = client.post("/auth/sign-up") {
            setBody(SignUpRequest("member2@example.com", "password123"))
        }
        assertEquals(HttpStatusCode.Created, signUp.status)
        val cookie = signUp.headers[HttpHeaders.SetCookie]
            ?.substringBefore(';')
            ?.substringAfter('=')
        assertNotNull(cookie)

        val token = cookie // jwt token is also returned in session in this app

        val createResp = client.post("/households") {
            headers.append("Authorization", "Bearer $token")
            setBody(CreateHouseholdRequest("Maple House"))
        }

        assertEquals(HttpStatusCode.Created, createResp.status)

        val createBody = createResp.body<CreateHouseholdResponse>()

        val joinResp = client.post("/households/join") {
            headers.append("Authorization", "Bearer $token")
            setBody(JoinHouseholdRequest(createBody.inviteCode!!))
        }

        println(joinResp.status)
        assert(joinResp.status == HttpStatusCode.OK)
    }
}
