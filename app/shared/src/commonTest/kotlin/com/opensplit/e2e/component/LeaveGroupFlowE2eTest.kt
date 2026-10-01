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

class LeaveGroupFlowE2eTest : BehaviorSpec() {
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

        And("user navigates to settings and clicks leave group") {
          beforeEach {
            val settingsComponent =
                koin
                    .get<GroupSettingsComponentFactory>()
                    .create(
                        cContext = context,
                        config = GroupSettingsComponent.Config("group-1"),
                    )
            settingsComponent.onLeaveGroupClicked()
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
