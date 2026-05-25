package com.opensplit.features

import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class HouseholdScenarios {
    @Test
    fun createAndJoinHousehold() = testOpenSplit {
        val createBody = client.post("/households") {
            setBody(CreateHouseholdRequest("Maple House"))
        }.also {
            assertEquals(HttpStatusCode.Created, it.status)
        }.body<CreateHouseholdResponse>()

        val joinResp = client.post("/households/join") {
            setBody(JoinHouseholdRequest(createBody.inviteCode!!))
        }

        println(joinResp.status)
        assert(joinResp.status == HttpStatusCode.OK)
    }
}
