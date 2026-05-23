package com.opensplit.features.household

import com.arkivanov.decompose.router.stack.pushNew
import com.opensplit.component.CContext
import com.opensplit.root.Destination
import com.opensplit.root.DestinationConfig
import com.opensplit.validation.household.HouseholdValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
enum class HouseholdMode {
    Create,
    Join,
}

data class HouseholdViewState(
    val mode: HouseholdMode = HouseholdMode.Create,
    val householdName: String = "",
    val inviteCode: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val isSubmitting: Boolean = false,
    val householdId: String? = null,
)

interface HouseholdComponent : Destination {
    val uiState: StateFlow<HouseholdViewState>
    fun useCreate()
    fun useJoin()
    fun updateHouseholdName(name: String)
    fun updateInviteCode(code: String)
    suspend fun submit()

    @Serializable
    data class Config(
        val accessToken: String,
        val mode: HouseholdMode = HouseholdMode.Create,
    ) : DestinationConfig {
        override val componentClass: KClass<out Any> = HouseholdComponent::class
    }
}

class DefaultHouseholdComponent(
    context: CContext,
    config: HouseholdComponent.Config,
    private val gateway: HouseholdGateway,
) : HouseholdComponent, CContext by context {

    private val accessToken = config.accessToken
    private val _uiState = MutableStateFlow(HouseholdViewState(mode = config.mode))
    override val uiState: StateFlow<HouseholdViewState> = _uiState

    override fun useCreate() {
        _uiState.update {
            it.copy(mode = HouseholdMode.Create, fieldErrors = emptyMap(), generalError = null)
        }
    }

    override fun useJoin() {
        _uiState.update {
            it.copy(mode = HouseholdMode.Join, fieldErrors = emptyMap(), generalError = null)
        }
    }

    override fun updateHouseholdName(name: String) {
        _uiState.update {
            it.copy(
                householdName = name,
                fieldErrors = it.fieldErrors - "name",
                generalError = null,
            )
        }
    }

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

        val validation = when (current.mode) {
            HouseholdMode.Create -> HouseholdValidation.validateCreateHousehold(current.householdName)
            HouseholdMode.Join -> HouseholdValidation.validateJoinHousehold(current.inviteCode)
        }

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
            val householdId = when (current.mode) {
                HouseholdMode.Create -> {
                    val result = gateway.createHousehold(current.householdName, accessToken)
                    result.id
                }
                HouseholdMode.Join -> {
                    val result = gateway.joinHousehold(current.inviteCode, accessToken)
                    result.householdId
                }
            }
            _uiState.update {
                it.copy(householdId = householdId, isSubmitting = false)
            }
        } catch (e: HouseholdRemoteException) {
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

class FakeHouseholdComponent(
    uiState: HouseholdViewState = HouseholdViewState(
        householdId = "household-1",
    )
) : HouseholdComponent {
    private val _uiState = MutableStateFlow(uiState)
    override val uiState: StateFlow<HouseholdViewState> = _uiState
    override fun useCreate() {}
    override fun useJoin() {}
    override fun updateHouseholdName(name: String) {}
    override fun updateInviteCode(code: String) {}
    override suspend fun submit() {}
}
