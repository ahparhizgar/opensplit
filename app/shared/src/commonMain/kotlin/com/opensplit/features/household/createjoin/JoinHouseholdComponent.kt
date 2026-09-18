package com.opensplit.features.household.createjoin

import com.ahparhizgar.katch.ApiCallError
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.remote.fieldErrors
import com.opensplit.remote.userMessage
import com.opensplit.repository.HouseholdRepository
import com.opensplit.root.TopLevelDestinationConfig
import com.opensplit.validation.household.HouseholdValidation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface JoinHouseholdComponent {
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

interface JoinHouseholdComponentFactory {
  fun create(cContext: CContext): JoinHouseholdComponent
}

class DefaultJoinHouseholdComponent(
    context: CContext,
    private val householdRepository: HouseholdRepository,
) : JoinHouseholdComponent, CContext by context {

  private val _uiState = MutableStateFlow(JoinHouseholdComponent.UiState())
  override val uiState: StateFlow<JoinHouseholdComponent.UiState> = _uiState

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
    val validation = HouseholdValidation.validateJoinHousehold(current.inviteCode)

    if (!validation.isValid) {
      _uiState.update {
        it.copy(fieldErrors = validation.errors, generalError = null, isSubmitting = false)
      }
      return@launch
    }

    _uiState.update { it.copy(fieldErrors = emptyMap(), generalError = null, isSubmitting = true) }

    try {
      val a = householdRepository.joinHousehold(current.inviteCode)
      _uiState.update { it.copy(isSubmitting = false) }
      navigation.replaceCurrent(HouseholdDetailsComponent.Config(a.id))
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

class DefaultJoinHouseholdComponentFactory(
    private val householdRepository: HouseholdRepository,
) : JoinHouseholdComponentFactory {
  override fun create(cContext: CContext): JoinHouseholdComponent =
      DefaultJoinHouseholdComponent(cContext, householdRepository)
}

class FakeJoinHouseholdComponent(
    uiState: JoinHouseholdComponent.UiState = JoinHouseholdComponent.UiState(),
) : JoinHouseholdComponent {
  private val _uiState = MutableStateFlow(uiState)
  override val uiState: StateFlow<JoinHouseholdComponent.UiState> = _uiState

  override fun updateInviteCode(code: String) {}

  override fun submit() = Job()

  override fun onBackClicked() {}
}
