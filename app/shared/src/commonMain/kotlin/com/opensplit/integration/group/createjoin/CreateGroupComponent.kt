package com.opensplit.integration.group.createjoin

import com.ahparhizgar.katch.ApiCallError
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.integration.group.details.GroupDetailsComponent
import com.opensplit.remote.fieldErrors
import com.opensplit.remote.userMessage
import com.opensplit.repository.GroupRepository
import com.opensplit.root.TopLevelDestinationConfig
import com.opensplit.validation.group.GroupValidation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

data class CreateGroupViewState(
    val groupName: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val isSubmitting: Boolean = false,
)

interface CreateGroupComponent {
  val uiState: StateFlow<CreateGroupViewState>

  fun updateGroupName(name: String)

  fun submit(): Job

  fun onBackClicked()

  @Serializable class Config : TopLevelDestinationConfig
}

interface CreateGroupComponentFactory {
  fun create(cContext: CContext): CreateGroupComponent
}

class DefaultCreateGroupComponent(
    context: CContext,
    private val groupRepository: GroupRepository,
) : CreateGroupComponent, CContext by context {

  private val _uiState = MutableStateFlow(CreateGroupViewState())
  override val uiState: StateFlow<CreateGroupViewState> = _uiState

  override fun updateGroupName(name: String) {
    _uiState.update {
      it.copy(
          groupName = name,
          fieldErrors = it.fieldErrors - "name",
          generalError = null,
      )
    }
  }

  val scope = componentScope()

  override fun submit() = scope.launch {
    val current = _uiState.value
    val validation = GroupValidation.validateCreateGroup(current.groupName)

    if (!validation.isValid) {
      _uiState.update {
        it.copy(fieldErrors = validation.errors, generalError = null, isSubmitting = false)
      }
      return@launch
    }

    _uiState.update { it.copy(fieldErrors = emptyMap(), generalError = null, isSubmitting = true) }

    try {
      val result = groupRepository.createGroup(current.groupName)
      _uiState.update { it.copy(isSubmitting = false) }
      navigation.replaceCurrent(GroupDetailsComponent.Config(result.id))
    } catch (e: ApiCallError) {
      _uiState.update {
        it.copy(
            fieldErrors = e.fieldErrors,
            generalError = e.userMessage,
            isSubmitting = false,
        )
      }
    }
  }

  override fun onBackClicked() {
    navigation.pop()
  }
}

class DefaultCreateGroupComponentFactory(
    private val groupRepository: GroupRepository,
) : CreateGroupComponentFactory {
  override fun create(
      cContext: CContext,
  ): CreateGroupComponent = DefaultCreateGroupComponent(cContext, groupRepository)
}

class FakeCreateGroupComponent(
    uiState: CreateGroupViewState = CreateGroupViewState(),
) : CreateGroupComponent {
  private val _uiState = MutableStateFlow(uiState)
  override val uiState: StateFlow<CreateGroupViewState> = _uiState

  override fun updateGroupName(name: String) {}

  override fun submit() = Job()

  override fun onBackClicked() {}
}
