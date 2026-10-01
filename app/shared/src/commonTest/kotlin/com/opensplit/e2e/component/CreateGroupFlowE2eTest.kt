package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.db.GroupDao
import com.opensplit.integration.auth.TokenStorage
import com.opensplit.integration.group.createjoin.CreateGroupComponent
import com.opensplit.integration.group.createjoin.GroupSelectionComponent
import com.opensplit.integration.group.details.GroupFlowComponent
import com.opensplit.integration.group.my.MyGroupsListComponent
import com.opensplit.root.RootComponentFactory
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CreateGroupFlowE2eTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())

    Given("authenticated user on MyGroupsListComponent") {
      val koin by integrationKoin()
      val context by testValue { TestCContext() }
      val tokenStorage by testValue { koin.get<TokenStorage>() }
      val groupDao by testValue { koin.get<GroupDao>() }

      beforeEach { tokenStorage.saveAccessToken("valid-user-token") }

      val root by testValue { koin.get<RootComponentFactory>().create(context) }

      When("user clicks add group") {
        beforeEach {
          testCoroutineScheduler.advanceUntilIdle()
          root.assertMyGroupsList()

          val myGroups = root.childStack.value.active.instance as MyGroupsListComponent
          myGroups.onAddGroupClick()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("selection screen is displayed") {
          root.childStack.value.active.instance.shouldBeInstanceOf<GroupSelectionComponent>()
        }

        And("user selects create group") {
          beforeEach {
            val selection =
                root.childStack.value.active.instance.shouldBeInstanceOf<GroupSelectionComponent>()
            selection.onCreateGroupClicked()
            testCoroutineScheduler.advanceUntilIdle()
          }

          Then("create group screen is displayed") {
            root.childStack.value.active.instance.shouldBeInstanceOf<CreateGroupComponent>()
          }

          And("user enters group name and submits") {
            beforeEach {
              val createGroup =
                  root.childStack.value.active.instance.shouldBeInstanceOf<CreateGroupComponent>()
              createGroup.updateGroupName("Amir's House")
              createGroup.submit().join()
              testCoroutineScheduler.advanceUntilIdle()
            }

            Then("group is persisted in local database") {
              val savedGroup = groupDao.getGroup("group-3")
              savedGroup.shouldNotBeNull()
              savedGroup.name shouldBe "Amir's House"
            }

            Then("navigation routes to GroupFlowComponent with created groupId") {
              val groupFlow =
                  root.childStack.value.active.instance.shouldBeInstanceOf<GroupFlowComponent>()
              groupFlow.groupId shouldBe "group-3"
            }
          }
        }
      }
    }
  }
}
