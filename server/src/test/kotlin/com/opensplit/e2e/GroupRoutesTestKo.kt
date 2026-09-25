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
import com.opensplit.util.KtorBehaviorSpec
import com.opensplit.util.createClient
import com.opensplit.util.createClientWithResult
import com.opensplit.util.testValue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.time.Clock

class GroupRoutesTestKo : KtorBehaviorSpec() {
  init {
    Given("groups endpoints") {
      When("creating and joining a group via invite link") {
        val createdGroup by testValue {
          client.post("/groups") { setBody(CreateGroupRequest("Maple House")) }.body<GroupDto>()
        }
        val joinResponse by testValue {
          client.post("/groups/memberships") {
            setBody(JoinGroupRequest(inviteCodeOrIdOrLink = createdGroup.inviteLink))
          }
        }
        Then("returns 200 OK for join request") {
          createdGroup.name shouldBe "Maple House"
          joinResponse.status shouldBe HttpStatusCode.OK
        }
      }

      When("querying a non-existent group ID") {
        val response by testValue { client.get("/groups/123456789") }
        Then("it returns 404 Not Found") { response.status shouldBe HttpStatusCode.NotFound }
      }

      When("querying a group the user does not belong to") {
        val fixture by testValue { createGroupWith2MembersFixture() }
        val response by testValue { client.get("/groups/${fixture.group.id}") }
        Then("it returns 404 Not Found") { response.status shouldBe HttpStatusCode.NotFound }
      }

      When("querying a group that the user owns") {
        val created by testValue { client.createGroup() }
        val response by testValue { client.get("/groups/${created.id}") }
        Then("it returns 200 OK with the group details") {
          response.status shouldBe HttpStatusCode.OK
          val fetched = response.body<GroupDto>()
          fetched.id shouldBe created.id
          fetched.name shouldBe created.name
        }
      }

      When("attempting to join with an invalid invite code") {
        val response by testValue {
          client.post("/groups/memberships") {
            setBody(JoinGroupRequest(inviteCodeOrIdOrLink = "not-a-real-code"))
          }
        }
        Then("returns 404 Not Found with clear error message") {
          response.status shouldBe HttpStatusCode.NotFound
          val error = response.body<ErrorResponse>()
          error.generalError shouldBe "Invalid invite code"
          error.errors["inviteCode"] shouldBe "Invalid invite code."
        }
      }

      When("group owner leaves while other members remain") {
        val fixture by testValue { createGroupWith2MembersFixture() }
        val leaveResponse by testValue {
          fixture.client1.delete("/groups/${fixture.group.id}/memberships")
        }
        val remainingUserGroups by testValue {
          leaveResponse // trigger leave first
          fixture.client2.getGroups()
        }
        Then("transfers ownership to the remaining member") {
          leaveResponse.status shouldBe HttpStatusCode.OK
          remainingUserGroups shouldHaveSize 1
          remainingUserGroups.first().isOwner shouldBe true
        }
      }

      When("group owner leaves as the last member") {
        val fixture by testValue { createGroupWith1MemberFixture() }
        val leaveResponse by testValue {
          fixture.client.delete("/groups/${fixture.group.id}/memberships")
        }
        val groupsAfterLeave by testValue {
          leaveResponse // trigger leave first
          fixture.client.getGroups()
        }
        Then("group is removed for that user") {
          leaveResponse.status shouldBe HttpStatusCode.OK
          groupsAfterLeave.shouldBeEmpty()
        }
      }

      When("owner adds a member by email") {
        val fixture by testValue { createGroupWith1MemberFixture() }
        val otherUserPair by testValue { createClientWithResult() }
        val addMemberResponse by testValue {
          fixture.client.post("/groups/${fixture.group.id}/memberships") {
            setBody(AddMemberByEmailRequest(email = otherUserPair.second.email))
          }
        }
        Then("member is added to the group") {
          addMemberResponse.status shouldBe HttpStatusCode.OK
          val group = addMemberResponse.body<GroupDto>()
          group.members.any { it.email == otherUserPair.second.email } shouldBe true

          val groupsOfNewMember = otherUserPair.first.getGroups()
          groupsOfNewMember.any { it.id == fixture.group.id } shouldBe true
        }
      }

      When("a non-owner attempts to add a member by email") {
        val fixture by testValue { createGroupWith1MemberFixture() }
        val otherClient by testValue { createClient() }
        val response by testValue {
          otherClient.post("/groups/${fixture.group.id}/memberships") {
            setBody(AddMemberByEmailRequest(email = "target2@example.com"))
          }
        }
        Then("it returns 403 Forbidden") { response.status shouldBe HttpStatusCode.Forbidden }
      }

      When("creating and fetching a group with lastInteractionAt") {
        val now by testValue { Clock.System.now() }
        val created by testValue { client.createGroup() }
        val fetchedGroups by testValue { client.getGroups() }
        Then("lastInteractionAt is present and matches the current time") {
          (created.lastInteractionAt - now).inWholeSeconds shouldBe 0
          fetchedGroups.first().lastInteractionAt shouldBe created.lastInteractionAt
        }
      }
    }
  }
}
