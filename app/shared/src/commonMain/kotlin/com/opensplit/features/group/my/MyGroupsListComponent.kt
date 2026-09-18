package com.opensplit.features.group.my

import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.FakeGroupFactory
import com.opensplit.domain.Group
import com.opensplit.features.group.createjoin.GroupSelectionComponent
import com.opensplit.features.group.details.GroupDetailsComponent
import com.opensplit.features.profile.ProfileComponent
import com.opensplit.repository.GroupRepository
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface MyGroupsListComponent {
  val uiState: Value<MyGroupsUiState>
  val isSettledExpanded: Value<Boolean>

  fun leaveGroup(groupId: String): Job

  fun onAddGroupClick()

  fun onGroupClick(id: String) {}

  fun onAccountClick() {}

  fun onToggleSettledExpanded()

  @Serializable data object Config : TopLevelDestinationConfig
}

interface MyGroupsListComponentFactory {
  fun create(cContext: CContext): MyGroupsListComponent
}

data class MyGroupsUiState(
    val groups: List<Group> = emptyList(),
    val overallBalance: Double = 0.0,
    val overallCurrency: String = "IRR",
)

class DefaultMyGroupsListComponent(
    context: CContext,
    private val repository: GroupRepository,
) : MyGroupsListComponent, CContext by context {

  private val scope = componentScope()

  init {
    scope.launch { repository.getGroups().collect { updateState(it) } }
    doOnCreate { repository.refresh() }
  }

  private val _uiState = MutableValue(MyGroupsUiState())
  override val uiState: Value<MyGroupsUiState> = _uiState

  private val _isSettledExpanded = MutableValue(false)
  override val isSettledExpanded: Value<Boolean> = _isSettledExpanded

  private fun updateState(groups: List<Group>) {
    _uiState.update {
      it.copy(
          groups = groups,
          overallBalance = groups.sumOf { h -> h.balance },
          overallCurrency = "IRR",
      )
    }
  }

  override fun onGroupClick(id: String) {
    navigation.pushNew(GroupDetailsComponent.Config(id))
  }

  override fun onAccountClick() {
    navigation.pushNew(ProfileComponent.Config)
  }

  override fun leaveGroup(groupId: String) = scope.launch { repository.leaveGroup(groupId) }

  override fun onAddGroupClick() {
    navigation.pushNew(GroupSelectionComponent.Config())
  }

  override fun onToggleSettledExpanded() {
    _isSettledExpanded.value = !_isSettledExpanded.value
  }
}

class DefaultMyGroupsListComponentFactory(
    private val repository: GroupRepository,
) : MyGroupsListComponentFactory {
  override fun create(cContext: CContext): MyGroupsListComponent =
      DefaultMyGroupsListComponent(cContext, repository)
}

class FakeMyGroupsListComponent(
    uiState: MyGroupsUiState = MyGroupsUiState(groups = listOf(FakeGroupFactory.create())),
    override val isSettledExpanded: MutableValue<Boolean> = MutableValue(false),
) : MyGroupsListComponent {
  override val uiState: Value<MyGroupsUiState> = MutableValue(uiState)

  override fun leaveGroup(groupId: String) = Job()

  override fun onAddGroupClick() {}

  override fun onToggleSettledExpanded() {
    isSettledExpanded.value = !isSettledExpanded.value
  }
}
