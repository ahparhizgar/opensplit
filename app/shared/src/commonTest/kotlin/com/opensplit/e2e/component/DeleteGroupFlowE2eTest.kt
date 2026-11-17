package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.db.GroupDao
import com.opensplit.fake.FakeGroupApi
import com.opensplit.integration.auth.TokenStorage
import com.opensplit.integration.group.my.MyGroupsListComponent
import com.opensplit.integration.group.settings.GroupSettingsComponent
import com.opensplit.integration.group.settings.GroupSettingsComponentFactory
import com.opensplit.root.RootComponentFactory
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class DeleteGroupFlowE2eTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())

    Given("authenticated user in a group") {
      val koin by integrationKoin()
      val context by testValue { TestCContext() }
      val tokenStorage by testValue { koin.get<TokenStorage>() }
      val groupDao by testValue { koin.get<GroupDao>() }
      val fakeGroupApi by testValue { koin.get<FakeGroupApi>() }

      beforeEach { tokenStorage.saveAccessToken("valid-user-token") }

      val root by testValue { koin.get<RootComponentFactory>().create(context) }
      val settingsComponent by testValue {
        koin
            .get<GroupSettingsComponentFactory>()
            .create(
                cContext = context,
                config = GroupSettingsComponent.Config("group-1"),
            )
      }

      When("user views group list with initial groups") {
        beforeEach {
          testCoroutineScheduler.advanceUntilIdle()
          root.assertMyGroupsList()
        }

        Then("initial group is present in DB and list") {
          val myGroups =
              root.childStack.value.active.instance.shouldBeInstanceOf<MyGroupsListComponent>()
          myGroups.uiState.value.groups.any { it.id == "group-1" } shouldBe true
          groupDao.getGroup("group-1").shouldNotBeNull()
        }

        And("user navigates to settings and clicks delete group") {
          beforeEach {
            settingsComponent.onDeleteGroupClicked()
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("a delete confirmation is shown and the group is still there") {
            settingsComponent.uiState.value.showDeleteConfirmation shouldBe true
            fakeGroupApi.groups.any { it.id == "group-1" } shouldBe true
            groupDao.getGroup("group-1").shouldNotBeNull()
          }

          And("user dismisses the confirmation") {
            beforeEach {
              settingsComponent.onDismissDeleteDialog()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("confirmation is hidden and the group is kept") {
              settingsComponent.uiState.value.showDeleteConfirmation shouldBe false
              fakeGroupApi.groups.any { it.id == "group-1" } shouldBe true
              groupDao.getGroup("group-1").shouldNotBeNull()
            }
          }

          And("user confirms the deletion") {
            beforeEach {
              settingsComponent.onConfirmDeleteGroup()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("group is removed from remote API and local Room database") {
              fakeGroupApi.groups.any { it.id == "group-1" } shouldBe false
              groupDao.getGroup("group-1").shouldBeNull()
              val myGroups =
                  root.childStack.value.active.instance.shouldBeInstanceOf<MyGroupsListComponent>()
              myGroups.uiState.value.groups.map { it.id } shouldNotContain "group-1"
            }
          }
        }
      }
    }
  }
}
