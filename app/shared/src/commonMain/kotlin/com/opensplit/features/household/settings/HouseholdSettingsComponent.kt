package com.opensplit.features.household.settings

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.component.navigation
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.features.household.HouseholdService
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface HouseholdSettingsComponent {
    val householdId: String
    val uiState: StateFlow<UiState>

    fun onBack() {}
    fun onAddPeopleClicked()
    fun onInviteLinkClicked()
    fun onLeaveGroupClicked()
    fun onDeleteGroupClicked()

    @Serializable
    data class Config(val householdId: String) : TopLevelDestinationConfig

    interface Factory {
        fun create(cContext: CContext, config: Config): HouseholdSettingsComponent
    }

    data class UiState(
        val household: HouseholdDto? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
    )
}

class DefaultHouseholdSettingsComponent(
    context: CContext,
    config: HouseholdSettingsComponent.Config,
    private val householdService: HouseholdService,
) : HouseholdSettingsComponent, CContext by context {

    override val householdId: String = config.householdId
    private val _uiState = MutableStateFlow(HouseholdSettingsComponent.UiState(isLoading = true))
    override val uiState: StateFlow<HouseholdSettingsComponent.UiState> = _uiState

    init {
        doOnCreate {
            loadSettings()
        }
    }

    override fun onBack() {
        navigation.pop()
    }

    override fun onAddPeopleClicked() {
        // TODO: Implement add people
    }

    override fun onInviteLinkClicked() {
        // TODO: Implement invite link
    }

    override fun onLeaveGroupClicked() {
        componentScope().launch {
            try {
                householdService.leaveHousehold(householdId)
                navigation.pop()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to leave group: ${e.message}") }
            }
        }
    }

    override fun onDeleteGroupClicked() {
        // TODO: Implement delete group
    }

    private fun loadSettings() = componentScope().launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val response = householdService.getHousehold(householdId)
            _uiState.update {
                it.copy(
                    household = response,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.message ?: "Failed to load settings",
                    isLoading = false
                )
            }
        }
    }

    class Factory(
        private val householdService: HouseholdService,
    ) : HouseholdSettingsComponent.Factory {
        override fun create(
            cContext: CContext,
            config: HouseholdSettingsComponent.Config
        ): HouseholdSettingsComponent = DefaultHouseholdSettingsComponent(cContext, config, householdService)
    }
}

class FakeHouseholdSettingsComponent(
    override val householdId: String = "h1",
    uiState: HouseholdSettingsComponent.UiState = HouseholdSettingsComponent.UiState(
        household = com.opensplit.dto.household.FakeHouseholdDtoFactory.create(
            id = "h1",
            name = "Box Gym Bros",
            members = com.opensplit.dto.household.FakeHouseholdMemberDtoFactory.createList()
        )
    )
) : HouseholdSettingsComponent {
    override val uiState: StateFlow<HouseholdSettingsComponent.UiState> = MutableStateFlow(uiState)
    override fun onAddPeopleClicked() {}
    override fun onInviteLinkClicked() {}
    override fun onLeaveGroupClicked() {}
    override fun onDeleteGroupClicked() {}
}
