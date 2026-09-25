package com.opensplit

import com.opensplit.dto.auth.AuthResult
import com.opensplit.dto.group.CreateGroupRequest
import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.JoinGroupRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.server.testing.ClientProvider

suspend fun HttpClient.createGroup(name: String = "Home"): GroupDto =
    post("/groups") { setBody(CreateGroupRequest(name)) }.body<GroupDto>()

suspend fun HttpClient.joinGroup(inviteCodeOrIdOrLink: String) =
    post("/groups/memberships") { setBody(JoinGroupRequest(inviteCodeOrIdOrLink)) }

suspend fun HttpClient.getGroup(groupId: String) = get("/groups/$groupId").body<GroupDto>()

suspend fun HttpClient.getGroups() = get("/groups").body<List<GroupDto>>()

suspend fun ClientProvider.createGroupWith1MemberFixture(): GroupWith1MemberFixture {
  val (client1, user1) = createClientWithResult("UserA")
  val group = client1.post("/groups") { setBody(CreateGroupRequest("AB House")) }.body<GroupDto>()
  return GroupWith1MemberFixture(
      group = group,
      user = user1,
      client = client1,
  )
}

/**
 * Creates a group with two members and returns the group and the two members' auth results. user1
 * (client1) creates the group, and user2 (client2) joins the group using the invite link.
 */
suspend fun ClientProvider.createGroupWith2MembersFixture(): GroupAnd2MembersFixture {
  val (client1, user1) = createClientWithResult("UserA")
  val group = client1.post("/groups") { setBody(CreateGroupRequest("AB House")) }.body<GroupDto>()
  val (client2, user2) = createClientWithResult("UserB")
  client2.joinGroup(group.inviteLink)
  return GroupAnd2MembersFixture(
      group = group,
      user1 = user1,
      user2 = user2,
      client1 = client1,
      client2 = client2,
  )
}

class GroupWith1MemberFixture(
    val group: GroupDto,
    val user: AuthResult,
    val client: HttpClient,
)

class GroupAnd2MembersFixture(
    val group: GroupDto,
    val user1: AuthResult,
    val user2: AuthResult,
    val client1: HttpClient,
    val client2: HttpClient,
)
