package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.db.ExpenseDao
import com.opensplit.db.SyncQueueDao
import com.opensplit.dto.auth.UserProfile
import com.opensplit.dto.expense.SyncStatus
import com.opensplit.dto.group.FakeGroupDtoFactory
import com.opensplit.fake.FakeExpenseApi
import com.opensplit.fake.FakeGroupApi
import com.opensplit.fake.FakeSyncApi
import com.opensplit.integration.expense.AddExpenseComponentFactory
import com.opensplit.repository.GroupRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.sync.SyncManager
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class OfflineExpenseSyncFlowTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())

    Given("authenticated user in a group with offline network") {
      val koin by integrationKoin()
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      val fakeGroupApi by testValue { koin.get<FakeGroupApi>() }
      val groupRepo by testValue { koin.get<GroupRepository>() }
      val fakeExpenseApi by testValue { koin.get<FakeExpenseApi>() }
      val fakeSyncApi by testValue { koin.get<FakeSyncApi>() }
      val syncQueueDao by testValue { koin.get<SyncQueueDao>() }
      val expenseDao by testValue { koin.get<ExpenseDao>() }
      val syncManager by testValue { koin.get<SyncManager>() }

      val addExpenseComponent by testValue {
        koin
            .get<AddExpenseComponentFactory>()
            .create(
                TestCContext().resumed(),
                "group-1",
                null,
                onNavigateToPayerFlow = {},
                onNavigateToSplitFlow = {},
                onFinished = {},
            )
      }

      beforeEach {
        profileRepo.setProfile(UserProfile("user-1", "User 1", "user-1@example.com"))
        fakeGroupApi.groups =
            listOf(FakeGroupDtoFactory.create(id = "group-1", name = "Test House"))
        groupRepo.refresh()
        testCoroutineScheduler.advanceUntilIdle()

        // Network disconnected
        fakeExpenseApi.disconnect()
        fakeSyncApi.disconnect()
      }

      When("user creates an expense while offline") {
        beforeEach {
          addExpenseComponent.onTitleChanged("Offline Groceries")
          addExpenseComponent.onAmountChanged("75.0")
          addExpenseComponent.onSaveClicked().join()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("expense is saved locally with PENDING status and queued in outbox") {
          val queue = syncQueueDao.getQueue().first()
          queue.shouldNotBeEmpty()
          val queuedItem = queue.first { it.entityType == "EXPENSE" }

          val localExpense = expenseDao.getExpense(queuedItem.entityId)
          localExpense?.syncStatus shouldBe SyncStatus.PENDING
          localExpense?.title shouldBe "Offline Groceries"
          localExpense?.amount shouldBe 75.0

          // Remote API received nothing
          fakeExpenseApi.createdExpenses.shouldBeEmpty()
        }

        And("network reconnects and sync is triggered") {
          beforeEach {
            fakeExpenseApi.connect()
            fakeSyncApi.connect()
            syncManager.sync()
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("outbox queue is drained and remote API receives the expense") {
            val queue = syncQueueDao.getQueue().first()
            queue.shouldBeEmpty()

            fakeExpenseApi.createdExpenses shouldHaveSize 1
            val created = fakeExpenseApi.createdExpenses.first()
            created.title shouldBe "Offline Groceries"
            created.amount shouldBe 75.0

            val serverExpense = expenseDao.getExpense(created.id)
            serverExpense?.syncStatus shouldBe SyncStatus.SYNCED
          }
        }
      }
    }
  }
}
