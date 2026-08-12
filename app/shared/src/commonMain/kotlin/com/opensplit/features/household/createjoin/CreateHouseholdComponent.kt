package com.opensplit.features.household.createjoin

import com.ahparhizgar.katch.ApiCallError
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.Household
import com.opensplit.remote.fieldErrors
import com.opensplit.remote.userMessage
import com.opensplit.repository.HouseholdRepository
import com.opensplit.validation.household.HouseholdValidation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateHouseholdViewState(
    val householdName: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val isSubmitting: Boolean = false,
)

interface CreateHouseholdComponent {
  val uiState: StateFlow<CreateHouseholdViewState>

  fun updateHouseholdName(name: String)

  fun submit(): Job

  interface Factory {
    fun create(cContext: CContext, onDone: (Household) -> Unit): CreateHouseholdComponent
  }
}

class DefaultCreateHouseholdComponent(
    context: CContext,
    private val householdRepository: HouseholdRepository,
    private val onDone: (Household) -> Unit,
) : CreateHouseholdComponent, CContext by context {

  private val _uiState = MutableStateFlow(CreateHouseholdViewState())
  override val uiState: StateFlow<CreateHouseholdViewState> = _uiState

  override fun updateHouseholdName(name: String) {
    _uiState.update {
      it.copy(
          householdName = name,
          fieldErrors = it.fieldErrors - "name",
          generalError = null,
      )
    }
  }

  val scope = componentScope()

  override fun submit() = scope.launch {
    val current = _uiState.value
    val validation = HouseholdValidation.validateCreateHousehold(current.householdName)

    if (!validation.isValid) {
      _uiState.update {
        it.copy(fieldErrors = validation.errors, generalError = null, isSubmitting = false)
      }
      return@launch
    }

    _uiState.update { it.copy(fieldErrors = emptyMap(), generalError = null, isSubmitting = true) }

    try {
      val result = householdRepository.createHousehold(current.householdName)
      _uiState.update { it.copy(isSubmitting = false) }
      onDone(result)
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

  class Factory(
      private val householdRepository: HouseholdRepository,
  ) : CreateHouseholdComponent.Factory {
    override fun create(
        cContext: CContext,
        onDone: (Household) -> Unit,
    ): CreateHouseholdComponent =
        DefaultCreateHouseholdComponent(cContext, householdRepository, onDone)
  }
}

class FakeCreateHouseholdComponent(
    uiState: CreateHouseholdViewState = CreateHouseholdViewState(),
) : CreateHouseholdComponent {
  private val _uiState = MutableStateFlow(uiState)
  override val uiState: StateFlow<CreateHouseholdViewState> = _uiState

  override fun updateHouseholdName(name: String) {}

  override fun submit() = Job()
}
