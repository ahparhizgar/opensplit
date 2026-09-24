package com.opensplit.features.group.my

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.opensplit.domain.FakeGroupFactory
import com.opensplit.ui.OpenSplitTheme
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalTestApi::class)
class MyGroupsListScreenTest {

  @Test
  fun testSettledGroupWithRecentActivityStaysInActiveList() = runComposeUiTest {
    val now = Clock.System.now()
    val settledRecentGroup =
        FakeGroupFactory.create(
            id = "group-recent-settled",
            name = "Recent Settled Trip",
            balance = 0.0,
            lastInteractionAt = now - 2.days,
        )
    val settledOldGroup =
        FakeGroupFactory.create(
            id = "group-old-settled",
            name = "Old Settled Trip",
            balance = 0.0,
            lastInteractionAt = now - 10.days,
        )
    val activeGroup =
        FakeGroupFactory.create(
            id = "group-active",
            name = "Active Apartment",
            balance = 50.0,
            lastInteractionAt = now - 10.days,
        )

    val fakeComponent =
        FakeMyGroupsListComponent(
            uiState =
                MyGroupsUiState(
                    groups = listOf(settledRecentGroup, settledOldGroup, activeGroup),
                ),
        )

    setContent { OpenSplitTheme { MyGroupsListScreen(component = fakeComponent) } }

    // Active group and recent settled group should be directly visible in active section
    onNode(hasTestTag("group-card-group-recent-settled")).assertIsDisplayed()
    onNode(hasTestTag("group-card-group-active")).assertIsDisplayed()

    // Old settled group should not be in active section (hidden under settled toggle)
    onNode(hasTestTag("group-card-group-old-settled")).assertDoesNotExist()
    onNode(hasText("Show 1 settled-up groups")).assertIsDisplayed()
  }
}
