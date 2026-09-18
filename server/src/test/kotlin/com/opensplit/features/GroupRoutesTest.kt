package com.opensplit.features

import com.opensplit.createAuthenticatedClient
import com.opensplit.createOtherClient
import com.opensplit.dto.auth.AuthResult
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.dto.group.AddMemberByEmailRequest
import com.opensplit.dto.group.CreateGroupRequest
import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.JoinGroupRequest
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

class GroupScenarios {
  @Test
  fun createAndJoinGroup() = testOpenSplit {
    val created =
        client
            .post("/groups") { setBody(CreateGroupRequest("Maple House")) }
            .also { assertEquals(HttpStatusCode.Created, it.status) }
            .body<GroupDto>()

    client
        .post("/groups/memberships") {
          setBody(JoinGroupRequest(inviteCodeOrIdOrLink = created.inviteLink))
        }
        .also { assertEquals(HttpStatusCode.OK, it.status) }
  }

  @Test
  fun getGroup_notFound() = testOpenSplit {
    val r = client.get("/groups/123456789")
    assertEquals(HttpStatusCode.NotFound, r.status)
  }

  @Test
  fun getNotMineGroup_notFound() = testOpenSplit {
    val created =
        client.post("/groups") { setBody(CreateGroupRequest("Maple House")) }.body<GroupDto>()

    val otherClient = createOtherClient()

    val r = otherClient.get("/groups/${created.id}")
    assertEquals(HttpStatusCode.NotFound, r.status)
  }

  @Test
  fun getGroup_returns() = testOpenSplit {
    val created =
        client.post("/groups") { setBody(CreateGroupRequest("Maple House")) }.body<GroupDto>()

    val r = client.get("/groups/${created.id}")
    assertEquals(HttpStatusCode.OK, r.status)
    val group = r.body<GroupDto>()
    assertEquals(created.id, group.id)
    assertEquals(created.name, group.name)
  }

  @Test
  fun joinReturnsClearErrorForInvalidInviteCode() = testOpenSplit {
    val response =
        client.post("/groups/memberships") {
          setBody(JoinGroupRequest(inviteCodeOrIdOrLink = "not-a-real-code"))
        }

    assertEquals(HttpStatusCode.NotFound, response.status)
    val error = response.body<ErrorResponse>()
    assertEquals("Invalid invite code", error.generalError)
    assertEquals("Invalid invite code.", error.errors["inviteCode"])
  }

  @Test
  fun leavingLastGroupReturnsSafeLandingState() = testOpenSplit {
    val created = client.post("/groups") { setBody(CreateGroupRequest("My Home")) }.body<GroupDto>()

    val otherUser =
        client
            .post("/users") {
              setBody(SignUpRequest("leave-test@example.com", "password123", "Amir"))
            }
            .body<AuthResult>()
    val otherClient = createAuthenticatedClient(otherUser.accessToken)

    otherClient
        .post("/groups/memberships") {
          setBody(JoinGroupRequest(inviteCodeOrIdOrLink = created.inviteLink))
        }
        .also { assertEquals(HttpStatusCode.OK, it.status) }

    val leaveResponse = otherClient.delete("/groups/${created.id}/memberships")
    assertEquals(HttpStatusCode.OK, leaveResponse.status)
    val afterLeave = leaveResponse.body<List<GroupDto>>()
    assertEquals(0, afterLeave.size)
  }

  @Test
  fun joinByGroupIdRequiresMembership() = testOpenSplit {
    val created =
        client.post("/groups") { setBody(CreateGroupRequest("Maple House")) }.body<GroupDto>()

    val otherUser =
        client
            .post("/users") {
              setBody(SignUpRequest("member-check@example.com", "password123", "Amir"))
            }
            .body<AuthResult>()
    val otherClient = createAuthenticatedClient(otherUser.accessToken)

    val joinById =
        otherClient.post("/groups/memberships") {
          setBody(JoinGroupRequest(inviteCodeOrIdOrLink = created.id))
        }

    assertEquals(HttpStatusCode.Forbidden, joinById.status)
    val error = joinById.body<ErrorResponse>()
    assertEquals("Missing permission to access this group", error.errors["permission"])
  }

  @Test
  fun overviewIncludesInviteLink() = testOpenSplit {
    client.post("/groups") { setBody(CreateGroupRequest("Family Home")) }.body<GroupDto>()

    val groups = client.get("/groups").body<List<GroupDto>>()

    assertEquals(1, groups.size)
    assertTrue(groups.first().inviteLink.isNotEmpty())
  }

  @Test
  fun ownerLeavesWithOtherMembersTransfersOwnership() = testOpenSplit {
    val created =
        client.post("/groups") { setBody(CreateGroupRequest("Our Home")) }.body<GroupDto>()

    val otherUser =
        client
            .post("/users") {
              setBody(SignUpRequest("owner-transfer@example.com", "password123", "Amir"))
            }
            .body<AuthResult>()
    val otherClient = createAuthenticatedClient(otherUser.accessToken)

    otherClient
        .post("/groups/memberships") {
          setBody(JoinGroupRequest(inviteCodeOrIdOrLink = created.inviteLink))
        }
        .also { assertEquals(HttpStatusCode.OK, it.status) }

    // Owner leaves, ownership should transfer
    client.delete("/groups/${created.id}/memberships").also {
      assertEquals(HttpStatusCode.OK, it.status)
    }

    // Verify other user is still in the group
    val groups = otherClient.get("/groups").body<List<GroupDto>>()
    assertEquals(1, groups.size)
    assertTrue(groups.first().isOwner, "Ownership should have been transferred")
  }

  @Test
  fun ownerLeavesAsLastMemberGroupBecomesOwnerless() = testOpenSplit {
    val created =
        client.post("/groups") { setBody(CreateGroupRequest("Solo Home")) }.body<GroupDto>()

    client.delete("/groups/${created.id}/memberships").also {
      assertEquals(HttpStatusCode.OK, it.status)
    }

    // Verify safe landing
    val groups = client.get("/groups").body<List<GroupDto>>()
    assertEquals(0, groups.size, "Should have no groups")
  }

  @Test
  fun addMemberByEmail() = testOpenSplit {
    val created =
        client.post("/groups") { setBody(CreateGroupRequest("Maple House")) }.body<GroupDto>()

    val otherUserEmail = "target@example.com"
    client.post("/users") { setBody(SignUpRequest(otherUserEmail, "password123", "Amir")) }

    val response =
        client.post("/groups/${created.id}/memberships") {
          setBody(AddMemberByEmailRequest(email = otherUserEmail))
        }

    assertEquals(HttpStatusCode.OK, response.status)
    val group = response.body<GroupDto>()
    assertTrue(group.members.any { it.email == otherUserEmail }, "Member should be added")
  }

  @Test
  fun addMemberByEmail_onlyOwner() = testOpenSplit {
    val created =
        client.post("/groups") { setBody(CreateGroupRequest("Maple House")) }.body<GroupDto>()

    val otherClient = createOtherClient()
    val targetEmail = "target2@example.com"
    client.post("/users") { setBody(SignUpRequest(targetEmail, "password123", "Amir")) }

    val response =
        otherClient.post("/groups/${created.id}/memberships") {
          setBody(AddMemberByEmailRequest(email = targetEmail))
        }

    assertEquals(HttpStatusCode.Forbidden, response.status)
  }
}
