package com.opensplit.features.household

import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdOverviewDto
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.features.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

interface HouseholdService {
  suspend fun createHousehold(name: String): HouseholdDto

  suspend fun joinHousehold(inviteCode: String): HouseholdDto

  suspend fun loadOverview(): HouseholdOverviewDto

  suspend fun leaveHousehold(householdId: String): HouseholdOverviewDto

  suspend fun getHousehold(id: String): HouseholdDto
}

class KtorHouseholdService(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : HouseholdService {

  private suspend fun handleUnauthorized() {
    tokenStorage.clearAccessToken()
  }

  override suspend fun createHousehold(name: String): HouseholdDto {
    val response =
        client.post("households") {
          contentType(ContentType.Application.Json)
          setBody(CreateHouseholdRequest(name = name))
        }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdDto>()
  }

  override suspend fun joinHousehold(inviteCode: String): HouseholdDto {
    val response =
        client.post("households/memberships") {
          contentType(ContentType.Application.Json)
          setBody<JoinHouseholdRequest>(
              JoinHouseholdRequest.ByInvite(inviteCodeOrIdOrLink = inviteCode)
          )
        }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdDto>()
  }

  override suspend fun loadOverview(): HouseholdOverviewDto {
    val response = client.get("households")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdOverviewDto>()
  }

  override suspend fun leaveHousehold(householdId: String): HouseholdOverviewDto {
    val response = client.delete("households/$householdId/memberships")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdOverviewDto>()
  }

  override suspend fun getHousehold(id: String): HouseholdDto {
    val response = client.get("households/$id")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdDto>()
  }
}
