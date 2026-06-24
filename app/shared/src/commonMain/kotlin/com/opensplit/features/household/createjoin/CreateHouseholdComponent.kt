package com.opensplit.features.household.createjoin

import com.arkivanov.decompose.router.stack.navigate
import com.opensplit.component.CContext
import com.opensplit.component.navigation
import com.opensplit.features.household.HouseholdService
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.remote.RemoteException
import com.opensplit.validation.household.HouseholdValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class CreateHouseholdViewState(
    val householdName: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val isSubmitting: Boolean = false,
    val householdId: String? = null,
)

interface CreateHouseholdComponent {
    val uiState: StateFlow<CreateHouseholdViewState>
    fun updateHouseholdName(name: String)
    suspend fun submit()
}

class DefaultCreateHouseholdComponent(
    context: CContext,
    private val gateway: HouseholdService,
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

    override suspend fun submit() {
        val current = _uiState.value
        val validation = HouseholdValidation.validateCreateHousehold(current.householdName)

        if (!validation.isValid) {
            _uiState.update {
                it.copy(fieldErrors = validation.errors, generalError = null, isSubmitting = false)
            }
            return
        }

        _uiState.update {
            it.copy(fieldErrors = emptyMap(), generalError = null, isSubmitting = true)
        }

        try {
            val result = gateway.createHousehold(current.householdName)
            _uiState.update { it.copy(householdId = result.id, isSubmitting = false) }
            navigation.navigate {
                it.dropLast(1) + HouseholdDetailsComponent.Config(result.id)
            }
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

class FakeCreateHouseholdComponent(
    uiState: CreateHouseholdViewState = CreateHouseholdViewState(),
) : CreateHouseholdComponent {
    private val _uiState = MutableStateFlow(uiState)
    override val uiState: StateFlow<CreateHouseholdViewState> = _uiState
    override fun updateHouseholdName(name: String) {}
    override suspend fun submit() {}
}
