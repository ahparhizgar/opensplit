package com.opensplit.e2e.component

import com.opensplit.component.TestCContext
import com.opensplit.db.GroupDao
import com.opensplit.db.GroupEntity
import com.opensplit.dto.auth.UserProfile
import com.opensplit.integration.auth.TokenStorage
import com.opensplit.integration.group.my.MyGroupsListComponent
import com.opensplit.integration.profile.ProfileComponent
import com.opensplit.repository.ProfileRepository
import com.opensplit.root.RootComponentFactory
import com.opensplit.util.MainDispatcherExtension
import com.opensplit.util.integrationKoin
import com.opensplit.util.testValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Clock
import kotlinx.coroutines.flow.first

class LogoutAndSessionClearFlowTest : BehaviorSpec() {
  init {
    extensions(MainDispatcherExtension())

    Given("authenticated user on MyGroupsListComponent with cached data") {
      val koin by integrationKoin()
      val context by testValue { TestCContext() }
      val tokenStorage by testValue { koin.get<TokenStorage>() }
      val profileRepo by testValue { koin.get<ProfileRepository>() }
      val groupDao by testValue { koin.get<GroupDao>() }
      val groupApi by testValue {
        koin.get<com.opensplit.integration.group.GroupApi>() as com.opensplit.fake.FakeGroupApi
      }

      beforeEach {
        groupApi.groups = emptyList()
        tokenStorage.saveAccessToken("active-user-token")
        profileRepo.setProfile(UserProfile("user-1", "Test User", "test@example.com"))
        groupDao.insertGroups(
            listOf(
                GroupEntity(
                    id = "group-1",
                    name = "Cached Group",
                    inviteLink = "",
                    isOwner = true,
                    lastInteractionAtEpochMillis = Clock.System.now(),
                )
            )
        )
      }

      var root by testValue { koin.get<RootComponentFactory>().create(context) }

      When("user navigates to profile and clicks logout") {
        beforeEach {
          testCoroutineScheduler.advanceUntilIdle()
          root.assertMyGroupsList()

          val myGroups = root.childStack.value.active.instance as MyGroupsListComponent
          myGroups.onAccountClick()
          testCoroutineScheduler.advanceUntilIdle()

          val profile = root.childStack.value.active.instance.shouldBeInstanceOf<ProfileComponent>()
          profile.onLogoutClicked().join()
          testCoroutineScheduler.advanceUntilIdle()
        }

        Then("token is cleared") { tokenStorage.getAccessToken().shouldBeNull() }

        And("session and database are cleared") {
          Then("profile is null") { profileRepo.profile.value.shouldBeNull() }

          Then("cached database tables are wiped") { groupDao.getGroups().first().shouldBeEmpty() }

          Then("navigation stack returns to AuthComponent") { root.assertAuth() }
        }
      }
    }
  }
}
