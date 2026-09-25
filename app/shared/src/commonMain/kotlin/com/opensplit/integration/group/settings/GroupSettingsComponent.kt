package com.opensplit.integration.group.settings

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.FakeGroupFactory
import com.opensplit.domain.Group
import com.opensplit.repository.GroupRepository
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface GroupSettingsComponent {
  val groupId: String
  val uiState: StateFlow<UiState>

  fun onBack() {}

  fun onInviteLinkClicked()

  fun onLeaveGroupClicked()

  fun onDeleteGroupClicked()

  @Serializable data class Config(val groupId: String) : TopLevelDestinationConfig

  data class UiState(
      val group: Group? = null,
      val isLoading: Boolean = false,
      val error: String? = null,
  )
}

interface GroupSettingsComponentFactory {
  fun create(
      cContext: CContext,
      config: GroupSettingsComponent.Config,
      onBack: (() -> Unit)? = null,
  ): GroupSettingsComponent
}

class DefaultGroupSettingsComponent(
    context: CContext,
    config: GroupSettingsComponent.Config,
    private val groupRepository: GroupRepository,
    private val onBack: (() -> Unit)? = null,
) : GroupSettingsComponent, CContext by context {

  override val groupId: String = config.groupId
  private val _uiState = MutableStateFlow(GroupSettingsComponent.UiState(isLoading = true))
  override val uiState: StateFlow<GroupSettingsComponent.UiState> = _uiState

  init {
    doOnCreate { loadSettings() }
  }

  override fun onBack() {
    if (onBack != null) {
      onBack.invoke()
    } else {
      navigation.pop()
    }
  }

  override fun onInviteLinkClicked() {
    // TODO: Implement invite link
  }

  override fun onLeaveGroupClicked() {
    componentScope().launch {
      try {
        groupRepository.leaveGroup(groupId)
        navigation.pop()
      } catch (e: Exception) {
        _uiState.update { it.copy(error = "Failed to leave group: ${e.message}") }
      }
    }
  }

  override fun onDeleteGroupClicked() {
    // TODO: Implement delete group
  }

  private fun loadSettings() =
      componentScope().launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
          val response = groupRepository.getGroup(groupId)
          _uiState.update { it.copy(group = response, isLoading = false) }
        } catch (e: Exception) {
          _uiState.update {
            it.copy(error = e.message ?: "Failed to load settings", isLoading = false)
          }
        }
      }
}

class DefaultGroupSettingsComponentFactory(
    private val groupRepository: GroupRepository,
) : GroupSettingsComponentFactory {
  override fun create(
      cContext: CContext,
      config: GroupSettingsComponent.Config,
      onBack: (() -> Unit)?,
  ): GroupSettingsComponent =
      DefaultGroupSettingsComponent(cContext, config, groupRepository, onBack)
}

class FakeGroupSettingsComponent(
    override val groupId: String = "h1",
    uiState: GroupSettingsComponent.UiState =
        GroupSettingsComponent.UiState(group = FakeGroupFactory.create(id = "h1")),
) : GroupSettingsComponent {
  override val uiState: StateFlow<GroupSettingsComponent.UiState> = MutableStateFlow(uiState)

  override fun onInviteLinkClicked() {}

  override fun onLeaveGroupClicked() {}

  override fun onDeleteGroupClicked() {}
}
