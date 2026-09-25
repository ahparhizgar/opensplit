package com.opensplit.e2e

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.group.AddMemberByEmailRequest
import com.opensplit.dto.group.CreateGroupRequest
import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.JoinGroupRequest
import com.opensplit.fixture.createGroup
import com.opensplit.fixture.createGroupWith1MemberFixture
import com.opensplit.fixture.createGroupWith2MembersFixture
import com.opensplit.fixture.getGroups
import com.opensplit.util.createClient
import com.opensplit.util.createClientWithResult
import com.opensplit.util.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class GroupRoutesTest {
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
    val fixture = createGroupWith2MembersFixture()

    val r = client.get("/groups/${fixture.group.id}")
    assertEquals(HttpStatusCode.NotFound, r.status)
  }

  @Test
  fun getGroup_returns() = testOpenSplit {
    val created = client.createGroup()

    val r = client.get("/groups/${created.id}")

    assertEquals(HttpStatusCode.OK, r.status)
    val fetched = r.body<GroupDto>()
    assertEquals(created.id, fetched.id)
    assertEquals(created.name, fetched.name)
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
  fun ownerLeavesWithOtherMembersTransfersOwnership() = testOpenSplit {
    val f = createGroupWith2MembersFixture()

    // Owner leaves
    f.client1.delete("/groups/${f.group.id}/memberships").also {
      assertEquals(HttpStatusCode.OK, it.status)
    }

    // Verify other user is still in the group
    val groups = f.client2.getGroups()
    assertEquals(1, groups.size)
    assertTrue(groups.first().isOwner, "Ownership should have been transferred")
  }

  @Test
  fun ownerLeavesAsLastMemberGroupBecomesOwnerless() = testOpenSplit {
    val f = createGroupWith1MemberFixture()

    f.client.delete("/groups/${f.group.id}/memberships")

    // Verify safe landing
    val groups = f.client.getGroups()

    assertEquals(0, groups.size, "Should have no groups")
  }

  @Test
  fun addMemberByEmail() = testOpenSplit {
    val f = createGroupWith1MemberFixture()

    val (otherClient, otherUser) = createClientWithResult()

    val response =
        f.client.post("/groups/${f.group.id}/memberships") {
          setBody(AddMemberByEmailRequest(email = otherUser.email))
        }

    assertEquals(HttpStatusCode.OK, response.status)
    val group = response.body<GroupDto>()
    assertTrue(
        group.members.any { it.email == otherUser.email },
        "Member should be added for creator",
    )

    val groupsOfNewMember = otherClient.getGroups()
    assertTrue(
        groupsOfNewMember.any { it.id == f.group.id },
        "Member should be added for new member",
    )
  }

  @Test
  fun addMemberByEmail_onlyOwner() = testOpenSplit {
    val f = createGroupWith1MemberFixture()
    val otherClient = createClient()
    val targetEmail = "target2@example.com"

    val response =
        otherClient.post("/groups/${f.group.id}/memberships") {
          setBody(AddMemberByEmailRequest(email = targetEmail))
        }

    assertEquals(HttpStatusCode.Forbidden, response.status)
  }

  @Test
  fun createAndFetchGroup_returnsLastInteractionAt() = testOpenSplit {
    val created = client.createGroup()
    val now = Clock.System.now()

    assertEquals((created.lastInteractionAt - now).inWholeSeconds, 0)
  }
}
