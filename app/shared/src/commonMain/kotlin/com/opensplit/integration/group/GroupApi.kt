package com.opensplit.integration.group

import com.opensplit.dto.group.AddMemberByEmailRequest
import com.opensplit.dto.group.CreateGroupRequest
import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.JoinGroupRequest
import com.opensplit.integration.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

interface GroupApi {
  suspend fun createGroup(name: String): GroupDto

  suspend fun joinGroup(inviteCode: String): GroupDto

  suspend fun addMemberByEmail(groupId: String, email: String): GroupDto

  suspend fun getGroups(): List<GroupDto>

  suspend fun leaveGroup(groupId: String): List<GroupDto>

  suspend fun getGroup(id: String): GroupDto
}

class KtorGroupApi(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : GroupApi {

  private suspend fun handleUnauthorized() {
    tokenStorage.clearAccessToken()
  }

  override suspend fun createGroup(name: String): GroupDto {
    val response = client.post("groups") { setBody(CreateGroupRequest(name = name)) }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<GroupDto>()
  }

  override suspend fun joinGroup(inviteCode: String): GroupDto {
    val response =
        client.post("groups/memberships") {
          setBody(JoinGroupRequest(inviteCodeOrIdOrLink = inviteCode))
        }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<GroupDto>()
  }

  override suspend fun addMemberByEmail(groupId: String, email: String): GroupDto {
    val response =
        client.post("groups/$groupId/memberships") {
          setBody(AddMemberByEmailRequest(email = email))
        }
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<GroupDto>()
  }

  override suspend fun getGroups(): List<GroupDto> {
    val response = client.get("groups")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<List<GroupDto>>()
  }

  override suspend fun leaveGroup(groupId: String): List<GroupDto> {
    val response = client.delete("groups/$groupId/memberships")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<List<GroupDto>>()
  }

  override suspend fun getGroup(id: String): GroupDto {
    val response = client.get("groups/$id")
    if (response.status == HttpStatusCode.Unauthorized) handleUnauthorized()
    return response.body<GroupDto>()
  }
}
