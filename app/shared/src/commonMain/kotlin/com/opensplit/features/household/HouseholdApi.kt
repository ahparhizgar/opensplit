package com.opensplit.features.household

import com.opensplit.dto.household.AddMemberByEmailRequest
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.features.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

interface HouseholdApi {
  suspend fun createHousehold(name: String): HouseholdDto

  suspend fun joinHousehold(inviteCode: String): HouseholdDto

  suspend fun addMemberByEmail(householdId: String, email: String): HouseholdDto

  suspend fun loadOverview(): List<HouseholdSummaryDto>

  suspend fun leaveHousehold(householdId: String): List<HouseholdSummaryDto>

  suspend fun getHousehold(id: String): HouseholdDto
}

class KtorHouseholdApi(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : HouseholdApi {

  private suspend fun handleUnauthorized() {
    tokenStorage.clearAccessToken()
  }

  override suspend fun createHousehold(name: String): HouseholdDto {
    val response = client.post("households") { setBody(CreateHouseholdRequest(name = name)) }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdDto>()
  }

  override suspend fun joinHousehold(inviteCode: String): HouseholdDto {
    val response =
        client.post("households/memberships") {
          setBody(JoinHouseholdRequest(inviteCodeOrIdOrLink = inviteCode))
        }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdDto>()
  }

  override suspend fun addMemberByEmail(householdId: String, email: String): HouseholdDto {
    val response =
        client.post("households/$householdId/memberships") {
          setBody(AddMemberByEmailRequest(email = email))
        }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdDto>()
  }

  override suspend fun loadOverview(): List<HouseholdSummaryDto> {
    val response = client.get("households")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<List<HouseholdSummaryDto>>()
  }

  override suspend fun leaveHousehold(householdId: String): List<HouseholdSummaryDto> {
    val response = client.delete("households/$householdId/memberships")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<List<HouseholdSummaryDto>>()
  }

  override suspend fun getHousehold(id: String): HouseholdDto {
    val response = client.get("households/$id")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<HouseholdDto>()
  }
}
