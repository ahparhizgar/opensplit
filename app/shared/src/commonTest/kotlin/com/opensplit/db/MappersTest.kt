package com.opensplit.db

import com.opensplit.domain.FakeParticipantShareFactory
import com.opensplit.dto.expense.FakeExpenseDtoFactory
import com.opensplit.dto.expense.FakeParticipantShareDtoFactory
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.group.FakeGroupDtoFactory
import com.opensplit.dto.group.FakeGroupMemberDtoFactory
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class MappersTest : BehaviorSpec() {
  init {
    Given("a group DTO") {
      val dto =
          FakeGroupDtoFactory.create(
              members =
                  listOf(
                      FakeGroupMemberDtoFactory.create(
                          id = "user-1",
                          balance = 10.15,
                          isCurrentUser = true,
                      ),
                      FakeGroupMemberDtoFactory.create(id = "user-2", balance = -10.15),
                  ),
          )

      When("it is stored as a group entity") {
        Then("the entity keeps the group fields") {
          val entity = dto.toEntity()
          entity.id shouldBe dto.id
          entity.name shouldBe dto.name
          entity.inviteLink shouldBe dto.inviteLink
          entity.isOwner shouldBe dto.isOwner
          entity.lastInteractionAtEpochMillis shouldBe dto.lastInteractionAt
        }

        Then("the entity keeps the owner flag") {
          dto.copy(isOwner = true).toEntity().isOwner shouldBe true
        }

        Then("converting the entity back gives the same DTO") {
          dto.toEntity().toDto(dto.members) shouldBe dto
        }
      }

      When("it is stored as group-with-members entities") {
        val stored =
            GroupWithMembers(
                group = dto.toEntity(),
                members = dto.members.map { it.toEntity(dto.id) },
            )

        Then("converting back to a DTO keeps every member field, including balance") {
          stored.toDto() shouldBe dto
        }

        Then("converting to a domain group keeps the members and their balances") {
          val group = stored.toDomain()
          group.id shouldBe dto.id
          group.name shouldBe dto.name
          group.inviteLink shouldBe dto.inviteLink
          group.members.map { it.userId to it.balance } shouldBe
              listOf("user-1" to 10.15, "user-2" to -10.15)
        }
      }

      When("it is converted to a domain group") {
        Then("the group balance is the current user's member balance") {
          dto.toDomain().balance shouldBe 10.15
        }

        Then("the group balance is zero when nobody is the current user") {
          val withoutCurrentUser =
              FakeGroupDtoFactory.create(
                  members =
                      listOf(
                          FakeGroupMemberDtoFactory.create(id = "user-1", balance = 10.15),
                          FakeGroupMemberDtoFactory.create(id = "user-2", balance = -10.15),
                      ),
              )
          withoutCurrentUser.toDomain().balance shouldBe 0.0
        }
      }
    }

    Given("a group member DTO") {
      val member =
          FakeGroupMemberDtoFactory.create(id = "user-1", balance = 10.15, isCurrentUser = true)

      When("it is stored as a member entity") {
        Then("the entity keeps every field, including balance") {
          val entity = member.toEntity("group-1")
          entity.groupId shouldBe "group-1"
          entity.userId shouldBe member.userId
          entity.name shouldBe member.name
          entity.email shouldBe member.email
          entity.isOwner shouldBe member.isOwner
          entity.isCurrentUser shouldBe member.isCurrentUser
          entity.balance shouldBe member.balance
        }

        Then("converting the entity back gives the same DTO") {
          member.toEntity("group-1").toDto() shouldBe member
        }

        Then("converting the entity to a domain member keeps every field") {
          val domainMember = member.toEntity("group-1").toDomain()
          domainMember.userId shouldBe member.userId
          domainMember.name shouldBe member.name
          domainMember.email shouldBe member.email
          domainMember.isOwner shouldBe member.isOwner
          domainMember.isCurrentUser shouldBe member.isCurrentUser
          domainMember.balance shouldBe member.balance
        }
      }

      When("it is converted to a domain member directly") {
        Then("every field is kept") {
          val domainMember = member.toDomain()
          domainMember.userId shouldBe member.userId
          domainMember.name shouldBe member.name
          domainMember.email shouldBe member.email
          domainMember.isOwner shouldBe member.isOwner
          domainMember.isCurrentUser shouldBe member.isCurrentUser
          domainMember.balance shouldBe member.balance
        }
      }
    }

    Given("an expense DTO") {
      val dto = FakeExpenseDtoFactory.create()

      When("it is stored as an expense entity") {
        Then("the entity keeps the expense fields") {
          val entity = dto.toEntity()
          entity.id shouldBe dto.id
          entity.groupId shouldBe dto.groupId
          entity.title shouldBe dto.title
          entity.amount shouldBe dto.amount
          entity.creator shouldBe dto.creator
          entity.createdAtEpochMillis shouldBe dto.createdAt
          entity.syncStatus shouldBe SyncStatus.SYNCED
        }

        Then("an explicit sync status is kept instead of the default") {
          dto.toEntity(SyncStatus.PENDING).syncStatus shouldBe SyncStatus.PENDING
        }

        Then("converting the entity back gives the same DTO") {
          dto.toEntity().toDto(dto.shares) shouldBe dto
        }
      }

      When("its split method is stored as JSON") {
        Then("every split method survives the round trip") {
          val splitMethods =
              listOf(
                  SplitMethod.Equally(listOf("user-1", "user-2")),
                  SplitMethod.Unequally(mapOf("user-1" to 12.5, "user-2" to 7.5)),
                  SplitMethod.Percentage(mapOf("user-1" to 60.0, "user-2" to 40.0)),
                  SplitMethod.Shares(mapOf("user-1" to 2, "user-2" to 1)),
                  SplitMethod.Adjustment(mapOf("user-1" to 5.0)),
              )
          splitMethods.forEach { splitMethod ->
            val stored = dto.copy(splitMethod = splitMethod).toEntity()
            stored.toDto(dto.shares).splitMethod shouldBe splitMethod
            stored.toDomain(dto.shares.map { it.toDomain() }).splitMethod shouldBe splitMethod
          }
        }
      }

      When("it is converted to a domain expense") {
        Then("the shares become participants and the other fields are kept") {
          val expense = dto.toDomain()
          expense.id shouldBe dto.id
          expense.groupId shouldBe dto.groupId
          expense.title shouldBe dto.title
          expense.amount shouldBe dto.amount
          expense.creator shouldBe dto.creator
          expense.createdAt shouldBe dto.createdAt
          expense.participants shouldBe dto.shares.map { it.toDomain() }
          expense.splitMethod shouldBe dto.splitMethod
          expense.syncStatus shouldBe dto.syncStatus
        }

        Then("reading it back from entities gives the same expense") {
          val participants = dto.shares.map { it.toDomain() }
          dto.toEntity().toDomain(participants) shouldBe dto.toDomain()
        }
      }
    }

    Given("a participant share DTO") {
      val share =
          FakeParticipantShareDtoFactory.create(
              userId = "user-1",
              paidShare = 10.0,
              consumedShare = 5.0,
          )

      When("it is stored as a participant entity") {
        Then("the entity keeps the expense id and the shares") {
          val entity = share.toEntity("expense-1")
          entity.expenseId shouldBe "expense-1"
          entity.userId shouldBe share.userId
          entity.paidShare shouldBe share.paidShare
          entity.consumedShare shouldBe share.consumedShare
        }

        Then("converting the entity back gives the same DTO") {
          share.toEntity("expense-1").toDto() shouldBe share
        }

        Then("converting the entity to a domain share keeps the values") {
          val domainShare = share.toEntity("expense-1").toDomain()
          domainShare.userId shouldBe share.userId
          domainShare.paidShare shouldBe share.paidShare
          domainShare.consumedShare shouldBe share.consumedShare
        }
      }
    }

    Given("a domain participant share") {
      val share =
          FakeParticipantShareFactory.create(
              userId = "user-1",
              paidShare = 10.0,
              consumedShare = 5.0,
          )

      When("it is stored as a participant entity") {
        Then("the entity keeps the expense id and the shares") {
          val entity = share.toEntity("expense-1")
          entity.expenseId shouldBe "expense-1"
          entity.userId shouldBe share.userId
          entity.paidShare shouldBe share.paidShare
          entity.consumedShare shouldBe share.consumedShare
        }

        Then("converting the entity back gives the same share") {
          share.toEntity("expense-1").toDomain() shouldBe share
        }
      }
    }
  }
}
