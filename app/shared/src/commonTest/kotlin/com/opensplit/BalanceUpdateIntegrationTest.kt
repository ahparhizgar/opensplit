package com.opensplit

import com.opensplit.domain.ParticipantShare
import com.opensplit.dto.auth.UserProfile
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.household.FakeHouseholdDtoFactory
import com.opensplit.fake.FakeHouseholdApi
import com.opensplit.fake.FakeSyncApi
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.HouseholdRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.sync.SyncManager
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.shouldBe
import kotlin.time.Clock

class BalanceUpdateIntegrationTest : BehaviorSpec() {
  init {
    extension(MainDispatcherExtension())
    val koin by integrationKoin()
    Given("a household with balance of zero") {
      val expenseRepo by testValue { koin.get<ExpenseRepository>() }
      val householdRepo by testValue { koin.get<HouseholdRepository>() }
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      val fakeHouseholdApi by testValue { koin.get<FakeHouseholdApi>() }
      val household1 by testValue {
        FakeHouseholdDtoFactory.create(
            id = "household-1",
            name = "Maple House",
        )
      }
      beforeEach {
        profileRepo.setProfile(UserProfile("user-1", "User 1", "user-1@example.com"))
        fakeHouseholdApi.households = listOf(household1)
        householdRepo.refresh()
        testCoroutineScheduler.advanceUntilIdle()
      }
      When("adding an expense") {
        beforeEach {
          expenseRepo.createExpense(
              householdId = "household-1",
              title = "Dinner",
              amount = 50.0,
              creator = household1.members.first().userId,
              shares =
                  listOf(
                      ParticipantShare("user-1", 50.0, 25.0),
                      ParticipantShare("user-2", 0.0, 25.0),
                  ),
              splitMethod = SplitMethod.Equally(household1.members.map { it.userId }),
          )
        }
        Then("the balance should update accordingly") {
          householdRepo
              .getHousehold("household-1")!!
              .members
              .first { it.userId == "user-1" }
              .balance shouldBe 25.0
        }
        And("synchronization occurs with edited shares from server") {
          beforeEach {
            koin.get<FakeSyncApi>().expenses =
                listOf(
                    ExpenseDto(
                        id = "expense-1",
                        householdId = "household-1",
                        title = "Dinner",
                        amount = 50.0,
                        creator = household1.members.first().userId,
                        shares =
                            listOf(
                                ParticipantShareDto("user-1", 50.0, 10.0),
                                ParticipantShareDto("user-2", 0.0, 40.0),
                            ),
                        splitMethod = SplitMethod.Equally(household1.members.map { it.userId }),
                        createdAt = Clock.System.now(),
                        syncStatus = SyncStatus.SYNCED,
                    )
                )
            testCoroutineScheduler.advanceUntilIdle()
            koin.get<SyncManager>().triggerSync()
            testCoroutineScheduler.advanceUntilIdle()
          }
          Then("the balance should be changed accordingly") {
            householdRepo
                .getHousehold("household-1")!!
                .members
                .first { it.userId == "user-1" }
                .balance shouldBe 40.0
          }
        }
      }

      When("a local transaction is added") {
        beforeEach {
          expenseRepo.createExpense(
              householdId = "household-1",
              title = "Groceries",
              amount = 20.0,
              creator = household1.members.first().userId,
              shares =
                  listOf(
                      ParticipantShare("user-1", 20.0, 10.0),
                      ParticipantShare("user-2", 0.0, 10.0),
                  ),
              splitMethod = SplitMethod.Equally(household1.members.map { it.userId }),
          )
        }
        And("households are refreshed") {
          beforeEach {
            fakeHouseholdApi
            householdRepo.refresh()
            testCoroutineScheduler.advanceUntilIdle()
          }
          Then("the balance should not be changed") {
            householdRepo
                .getHousehold("household-1")!!
                .members
                .first { it.userId == "user-1" }
                .balance shouldBe 10.0
          }
        }
      }
    }
  }
}
