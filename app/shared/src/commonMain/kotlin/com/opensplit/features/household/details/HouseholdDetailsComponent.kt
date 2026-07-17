package com.opensplit.features.household.details

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.features.household.HouseholdApi
import com.opensplit.features.household.settings.HouseholdSettingsComponent
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface HouseholdDetailsComponent {
  val householdId: String
  val uiState: StateFlow<UiState>

  fun onAddMemberClicked() {}

  fun onBack() {}

  fun onSettingsClick() {}

  @Serializable data class Config(val householdId: String) : TopLevelDestinationConfig

  interface Factory {
    fun create(cContext: CContext, config: Config): HouseholdDetailsComponent
  }

  data class UiState(
      val household: HouseholdDto? = null,
      val isLoading: Boolean = false,
      val error: String? = null,
  )
}

class DefaultHouseholdDetailsComponent(
    context: CContext,
    config: HouseholdDetailsComponent.Config,
    private val gateway: HouseholdApi,
) : HouseholdDetailsComponent, CContext by context {

  override val householdId: String = config.householdId
  private val _uiState = MutableStateFlow(HouseholdDetailsComponent.UiState(isLoading = true))
  override val uiState: StateFlow<HouseholdDetailsComponent.UiState> = _uiState

  init {
    doOnCreate { loadDetails() }
  }

  override fun onAddMemberClicked() {
    TODO()
  }

  override fun onSettingsClick() {
    navigation.pushNew(HouseholdSettingsComponent.Config(householdId))
  }

  override fun onBack() {
    navigation.pop()
  }

  private fun loadDetails() =
      componentScope().launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
          val response = gateway.getHousehold(householdId)
          _uiState.update { it.copy(household = response, isLoading = false) }
        } catch (e: Exception) {
          _uiState.update {
            it.copy(error = e.message ?: "Failed to load household details", isLoading = false)
          }
        }
      }

  class Factory(
      private val gateway: HouseholdApi,
  ) : HouseholdDetailsComponent.Factory {
    override fun create(
        cContext: CContext,
        config: HouseholdDetailsComponent.Config,
    ): HouseholdDetailsComponent = DefaultHouseholdDetailsComponent(cContext, config, gateway)
  }
}

class FakeHouseholdDetailsComponent(
    override val householdId: String = "h12345",
    uiState: HouseholdDetailsComponent.UiState =
        HouseholdDetailsComponent.UiState(
            HouseholdDto(
                id = householdId,
                name = "Fake Household",
                members = emptyList(),
                inviteLink = "https://opensplit.com/join/fake-code",
            )
        ),
) : HouseholdDetailsComponent {
  override val uiState: StateFlow<HouseholdDetailsComponent.UiState> = MutableStateFlow(uiState)
}
