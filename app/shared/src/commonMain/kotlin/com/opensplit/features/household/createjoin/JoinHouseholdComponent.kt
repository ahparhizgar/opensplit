package com.opensplit.features.household.createjoin

import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.opensplit.component.CContext
import com.opensplit.component.navigation
import com.opensplit.features.household.HouseholdService
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.remote.RemoteException
import com.opensplit.validation.household.HouseholdValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface JoinHouseholdComponent {
  val uiState: StateFlow<UiState>

  fun updateInviteCode(code: String)

  suspend fun submit()

  data class UiState(
      val inviteCode: String = "",
      val fieldErrors: Map<String, String> = emptyMap(),
      val generalError: String? = null,
      val isSubmitting: Boolean = false,
  )
}

class DefaultJoinHouseholdComponent(
    context: CContext,
    private val gateway: HouseholdService,
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

  override suspend fun submit() {
    val current = _uiState.value
    val validation = HouseholdValidation.validateJoinHousehold(current.inviteCode)

    if (!validation.isValid) {
      _uiState.update {
        it.copy(fieldErrors = validation.errors, generalError = null, isSubmitting = false)
      }
      return
    }

    _uiState.update { it.copy(fieldErrors = emptyMap(), generalError = null, isSubmitting = true) }

    try {
      val a = gateway.joinHousehold(current.inviteCode)
      _uiState.update { it.copy(isSubmitting = false) }
      navigation.replaceCurrent(HouseholdDetailsComponent.Config(a.id))
    } catch (e: RemoteException) {
      _uiState.update {
        it.copy(
            fieldErrors = e.fieldErrors,
            generalError = e.generalError,
            isSubmitting = false,
        )
      }
    }
  }
}

class FakeJoinHouseholdComponent(
    uiState: JoinHouseholdComponent.UiState = JoinHouseholdComponent.UiState(),
) : JoinHouseholdComponent {
  private val _uiState = MutableStateFlow(uiState)
  override val uiState: StateFlow<JoinHouseholdComponent.UiState> = _uiState

  override fun updateInviteCode(code: String) {}

  override suspend fun submit() {}
}
