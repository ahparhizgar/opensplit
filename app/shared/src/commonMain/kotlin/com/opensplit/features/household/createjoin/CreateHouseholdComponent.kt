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

  fun onBackClicked()

  @Serializable class Config : TopLevelDestinationConfig
}

interface CreateHouseholdComponentFactory {
  fun create(cContext: CContext): CreateHouseholdComponent
}

class DefaultCreateHouseholdComponent(
    context: CContext,
    private val householdRepository: HouseholdRepository,
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
      navigation.replaceCurrent(HouseholdDetailsComponent.Config(result.id))
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

class DefaultCreateHouseholdComponentFactory(
    private val householdRepository: HouseholdRepository,
) : CreateHouseholdComponentFactory {
  override fun create(
      cContext: CContext,
  ): CreateHouseholdComponent =
      DefaultCreateHouseholdComponent(cContext, householdRepository)
}

class FakeCreateHouseholdComponent(
    uiState: CreateHouseholdViewState = CreateHouseholdViewState(),
) : CreateHouseholdComponent {
  private val _uiState = MutableStateFlow(uiState)
  override val uiState: StateFlow<CreateHouseholdViewState> = _uiState

  override fun updateHouseholdName(name: String) {}

  override fun submit() = Job()

  override fun onBackClicked() {}
}
