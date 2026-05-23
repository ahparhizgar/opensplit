package com.opensplit.features.household

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.dto.household.JoinHouseholdResponse
import com.opensplit.features.auth.createAuthHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

data class HouseholdRemoteException(
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
) : RuntimeException(generalError ?: "Household request failed")

interface HouseholdGateway {
    suspend fun createHousehold(name: String, accessToken: String): CreateHouseholdResponse
    suspend fun joinHousehold(inviteCode: String, accessToken: String): JoinHouseholdResponse
}

class KtorHouseholdGateway(
    private val client: HttpClient,
    private val baseUrl: String,
) : HouseholdGateway {

    override suspend fun createHousehold(name: String, accessToken: String): CreateHouseholdResponse {
        val response = client.post("$baseUrl/households") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(CreateHouseholdRequest(name = name))
        }
        return parseResponse(response)
    }

    override suspend fun joinHousehold(inviteCode: String, accessToken: String): JoinHouseholdResponse {
        val response = client.post("$baseUrl/households/join") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(JoinHouseholdRequest(inviteCodeOrId = inviteCode))
        }
        return parseResponse(response)
    }

    private suspend inline fun <reified T> parseResponse(response: HttpResponse): T {
        if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.Created) {
            val error = runCatching { response.body<ErrorResponse>() }.getOrNull()
            throw HouseholdRemoteException(
                fieldErrors = error?.errors ?: emptyMap(),
                generalError = error?.errors?.values?.firstOrNull() ?: "Request failed",
            )
        }
        return try {
            response.body()
        } catch (e: Throwable) {
            throw HouseholdRemoteException(generalError = e.message ?: "Network error")
        }
    }
}

fun createHouseholdGateway(): HouseholdGateway =
    KtorHouseholdGateway(createAuthHttpClient(), "http://127.0.0.1:8080")
