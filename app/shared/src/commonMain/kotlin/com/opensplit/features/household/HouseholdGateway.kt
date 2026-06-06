package com.opensplit.features.household

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.HouseholdOverviewResponse
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.dto.household.JoinHouseholdResponse
import com.opensplit.dto.household.SwitchHouseholdRequest
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.auth.createAuthHttpClient
import com.opensplit.features.auth.getApiBaseUrl
import com.opensplit.features.auth.isJwtExpired
import com.opensplit.remote.RemoteException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

interface HouseholdGateway {
    suspend fun createHousehold(name: String): CreateHouseholdResponse
    suspend fun joinHousehold(inviteCode: String): JoinHouseholdResponse
    suspend fun loadOverview(): HouseholdOverviewResponse
    suspend fun switchHousehold(householdId: String): HouseholdOverviewResponse
    suspend fun leaveHousehold(householdId: String): HouseholdOverviewResponse
}

class KtorHouseholdGateway(
    private val tokenStorage: TokenStorage,
) : HouseholdGateway {

    private val baseUrl = getApiBaseUrl()

    private val client: HttpClient = createAuthHttpClient().config {
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(tokenStorage.getAccessToken() ?: "", "")
                }
            }
        }
    }

    private suspend fun checkTokenExpired(): Boolean {
        val token = tokenStorage.getAccessToken() ?: return false
        return isJwtExpired(token)
    }

    private suspend fun handleUnauthorized() {
        tokenStorage.clearAccessToken()
        throw RemoteException(generalError = "Session expired. Please sign in again.")
    }

    override suspend fun createHousehold(name: String): CreateHouseholdResponse {
        if (checkTokenExpired()) handleUnauthorized()
        val response = client.post("$baseUrl/households") {
            contentType(ContentType.Application.Json)
            setBody(CreateHouseholdRequest(name = name))
        }
        if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
        return parseResponse(response)
    }

    override suspend fun joinHousehold(inviteCode: String): JoinHouseholdResponse {
        if (checkTokenExpired()) handleUnauthorized()
        val response = client.post("$baseUrl/households/join") {
            contentType(ContentType.Application.Json)
            setBody(JoinHouseholdRequest(inviteCodeOrId = inviteCode))
        }
        if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
        return parseResponse(response)
    }

    override suspend fun loadOverview(): HouseholdOverviewResponse {
        if (checkTokenExpired()) handleUnauthorized()
        val response = client.get("$baseUrl/households/overview")
        if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
        return parseResponse(response)
    }

    override suspend fun switchHousehold(householdId: String): HouseholdOverviewResponse {
        if (checkTokenExpired()) handleUnauthorized()
        val response = client.post("$baseUrl/households/context") {
            contentType(ContentType.Application.Json)
            setBody(SwitchHouseholdRequest(householdId = householdId))
        }
        if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
        return parseResponse(response)
    }

    override suspend fun leaveHousehold(householdId: String): HouseholdOverviewResponse {
        if (checkTokenExpired()) handleUnauthorized()
        val response = client.delete("$baseUrl/households/$householdId/memberships/me")
        if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
        return parseResponse(response)
    }

    private suspend inline fun <reified T> parseResponse(response: HttpResponse): T {
        if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.Created) {
            val error = runCatching { response.body<ErrorResponse>() }.getOrNull()
            throw RemoteException(
                fieldErrors = error?.errors ?: emptyMap(),
                generalError = error?.errors?.values?.firstOrNull() ?: "Request failed",
            )
        }
        return try {
            response.body()
        } catch (e: Throwable) {
            throw RemoteException(generalError = e.message ?: "Network error")
        }
    }
}
