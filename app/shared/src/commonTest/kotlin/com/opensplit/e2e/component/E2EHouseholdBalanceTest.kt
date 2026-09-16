package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.dto.auth.UserProfile
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.household.FakeHouseholdDtoFactory
import com.opensplit.fake.FakeHouseholdApi
import com.opensplit.fake.FakeSyncApi
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.features.household.my.MyHouseholdsListComponent
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

class E2EHouseholdBalanceTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())
    val koin by integrationKoin()

    Given("a client app with household list and details components") {
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      val fakeHouseholdApi by testValue { koin.get<FakeHouseholdApi>() }
      val householdRepo by testValue { koin.get<HouseholdRepository>() }
      val fakeSyncApi by testValue { koin.get<FakeSyncApi>() }
      val syncManager by testValue { koin.get<SyncManager>() }

      val myHouseholdsListComponent by testValue {
        koin.get<MyHouseholdsListComponent.Factory>().create(TestCContext().resumed())
      }

      val householdDetailsComponent by testValue {
        koin
            .get<HouseholdDetailsComponent.Factory>()
            .create(TestCContext().resumed(), HouseholdDetailsComponent.Config("household-1"))
      }

      beforeEach {
        profileRepo.setProfile(UserProfile("user-1", "User 1", "user-1@example.com"))
        fakeHouseholdApi.households =
            listOf(FakeHouseholdDtoFactory.create(id = "household-1", name = "Test House"))
        fakeSyncApi.expenses = emptyList()
        fakeSyncApi.deletedExpenseIds = emptyList()
        householdRepo.refresh()
        testCoroutineScheduler.advanceUntilIdle()
      }

      Then("initial household balance is zero in both components") {
        myHouseholdsListComponent.uiState.value.households
            .first { it.id == "household-1" }
            .balance shouldBe 0.0
        householdDetailsComponent.uiState.value.household?.balance shouldBe 0.0
      }

      When("adding an expense locally") {
        beforeEach {
          val addExpenseComponent =
              koin
                  .get<AddExpenseComponent.Factory>()
                  .create(
                      TestCContext().resumed(),
                      AddExpenseComponent.Config("household-1"),
                      onFinished = {},
                  )
          testCoroutineScheduler.advanceUntilIdle()
          addExpenseComponent.onTitleChanged("Dinner")
          addExpenseComponent.onAmountChanged("60.0")
          addExpenseComponent.onSaveClicked().join()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("household balance is updated locally after add") {
          myHouseholdsListComponent.uiState.value.households
              .first { it.id == "household-1" }
              .balance shouldBe 30.0
          householdDetailsComponent.uiState.value.household?.balance shouldBe 30.0
        }

        And("updating the expense locally") {
          beforeEach {
            val targetExpenseId = householdDetailsComponent.uiState.value.expenses.first().id
            val editExpenseComponent =
                koin
                    .get<AddExpenseComponent.Factory>()
                    .create(
                        TestCContext().resumed(),
                        AddExpenseComponent.Config("household-1", targetExpenseId),
                        onFinished = {},
                    )
            testCoroutineScheduler.advanceUntilIdle()
            editExpenseComponent.onTitleChanged("Fancy Dinner")
            editExpenseComponent.onAmountChanged("100.0")
            editExpenseComponent.onSaveClicked().join()
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("household balance is updated locally after update") {
            myHouseholdsListComponent.uiState.value.households
                .first { it.id == "household-1" }
                .balance shouldBe 50.0
            householdDetailsComponent.uiState.value.household?.balance shouldBe 50.0
          }

          And("deleting the expense locally") {
            beforeEach {
              val targetExpenseId = householdDetailsComponent.uiState.value.expenses.first().id
              val expenseDetailsComponent =
                  koin
                      .get<ExpenseDetailsComponent.Factory>()
                      .create(
                          TestCContext().resumed(),
                          ExpenseDetailsComponent.Config("household-1", targetExpenseId),
                          onBack = {},
                      )
              testCoroutineScheduler.advanceUntilIdle()
              expenseDetailsComponent.onDeleteClicked()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("household balance is updated locally after delete") {
              myHouseholdsListComponent.uiState.value.households
                  .first { it.id == "household-1" }
                  .balance shouldBe 0.0
              householdDetailsComponent.uiState.value.household?.balance shouldBe 0.0
            }
          }
        }
      }

      When("adding an expense remotely via sync") {
        val serverExpense =
            ExpenseDto(
                id = "server-expense-1",
                householdId = "household-1",
                title = "Groceries",
                amount = 80.0,
                creator = "user-2",
                createdAt = Clock.System.now(),
                shares =
                    listOf(
                        ParticipantShareDto("user-1", 0.0, 40.0),
                        ParticipantShareDto("user-2", 80.0, 40.0),
                    ),
                splitMethod = SplitMethod.Equally(listOf("user-1", "user-2")),
                syncStatus = SyncStatus.SYNCED,
            )

        beforeEach {
          fakeSyncApi.expenses = listOf(serverExpense)
          syncManager.sync()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("household balance is updated remotely after add sync") {
          myHouseholdsListComponent.uiState.value.households
              .first { it.id == "household-1" }
              .balance shouldBe -40.0
          householdDetailsComponent.uiState.value.household?.balance shouldBe -40.0
        }

        And("updating the expense remotely via sync") {
          val updatedServerExpense =
              serverExpense.copy(
                  amount = 120.0,
                  shares =
                      listOf(
                          ParticipantShareDto("user-1", 0.0, 60.0),
                          ParticipantShareDto("user-2", 120.0, 60.0),
                      ),
              )

          beforeEach {
            fakeSyncApi.expenses = listOf(updatedServerExpense)
            syncManager.sync()
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("household balance is updated remotely after update sync") {
            myHouseholdsListComponent.uiState.value.households
                .first { it.id == "household-1" }
                .balance shouldBe -60.0
            householdDetailsComponent.uiState.value.household?.balance shouldBe -60.0
          }

          And("deleting the expense remotely via sync") {
            beforeEach {
              fakeSyncApi.expenses = emptyList()
              fakeSyncApi.deletedExpenseIds = listOf("server-expense-1")
              syncManager.sync()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("household balance is updated remotely after delete sync") {
              myHouseholdsListComponent.uiState.value.households
                  .first { it.id == "household-1" }
                  .balance shouldBe 0.0
              householdDetailsComponent.uiState.value.household?.balance shouldBe 0.0
            }
          }
        }
      }
    }
  }
}
