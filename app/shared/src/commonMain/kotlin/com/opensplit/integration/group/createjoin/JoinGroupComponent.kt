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

interface JoinGroupComponent {
  val uiState: StateFlow<UiState>

  fun updateInviteCode(code: String)

  fun submit(): Job

  fun onBackClicked()

  @Serializable class Config : TopLevelDestinationConfig

  data class UiState(
      val inviteCode: String = "",
      val fieldErrors: Map<String, String> = emptyMap(),
      val generalError: String? = null,
      val isSubmitting: Boolean = false,
  )
}

interface JoinGroupComponentFactory {
  fun create(cContext: CContext): JoinGroupComponent
}

class DefaultJoinGroupComponent(
    context: CContext,
    private val groupRepository: GroupRepository,
) : JoinGroupComponent, CContext by context {

  private val _uiState = MutableStateFlow(JoinGroupComponent.UiState())
  override val uiState: StateFlow<JoinGroupComponent.UiState> = _uiState

  override fun updateInviteCode(code: String) {
    _uiState.update {
      it.copy(
          inviteCode = code,
          fieldErrors = it.fieldErrors - "inviteCode",
          generalError = null,
      )
    }
  }

  val scope = componentScope()

  override fun submit() = scope.launch {
    val current = _uiState.value
    val validation = GroupValidation.validateJoinGroup(current.inviteCode)

    if (!validation.isValid) {
      _uiState.update {
        it.copy(fieldErrors = validation.errors, generalError = null, isSubmitting = false)
      }
      return@launch
    }

    _uiState.update { it.copy(fieldErrors = emptyMap(), generalError = null, isSubmitting = true) }

    try {
      val a = groupRepository.joinGroup(current.inviteCode)
      _uiState.update { it.copy(isSubmitting = false) }
      navigation.replaceCurrent(GroupDetailsComponent.Config(a.id))
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

class DefaultJoinGroupComponentFactory(
    private val groupRepository: GroupRepository,
) : JoinGroupComponentFactory {
  override fun create(cContext: CContext): JoinGroupComponent =
      DefaultJoinGroupComponent(cContext, groupRepository)
}

class FakeJoinGroupComponent(
    uiState: JoinGroupComponent.UiState = JoinGroupComponent.UiState(),
) : JoinGroupComponent {
  private val _uiState = MutableStateFlow(uiState)
  override val uiState: StateFlow<JoinGroupComponent.UiState> = _uiState

  override fun updateInviteCode(code: String) {}

  override fun submit() = Job()

  override fun onBackClicked() {}
}
