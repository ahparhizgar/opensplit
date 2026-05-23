package com.opensplit.features.household

import com.opensplit.component.CContext
import com.opensplit.remote.RemoteException
import com.opensplit.validation.household.HouseholdValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class JoinHouseholdViewState(
    val inviteCode: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val isSubmitting: Boolean = false,
    val householdId: String? = null,
)

interface JoinHouseholdComponent {
    val uiState: StateFlow<JoinHouseholdViewState>
    fun updateInviteCode(code: String)
    suspend fun submit()
}

class DefaultJoinHouseholdComponent(
    context: CContext,
    private val gateway: HouseholdGateway,
) : JoinHouseholdComponent, CContext by context {

    private val _uiState = MutableStateFlow(JoinHouseholdViewState())
    override val uiState: StateFlow<JoinHouseholdViewState> = _uiState

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

        _uiState.update {
            it.copy(fieldErrors = emptyMap(), generalError = null, isSubmitting = true)
        }

        try {
            val result = gateway.joinHousehold(current.inviteCode)
            _uiState.update { it.copy(householdId = result.householdId, isSubmitting = false) }
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
    uiState: JoinHouseholdViewState = JoinHouseholdViewState(),
) : JoinHouseholdComponent {
    private val _uiState = MutableStateFlow(uiState)
    override val uiState: StateFlow<JoinHouseholdViewState> = _uiState
    override fun updateInviteCode(code: String) {}
    override suspend fun submit() {}
}
