package com.opensplit.features.household.details

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.dto.expense.ExpenseDto
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.ExpenseApi
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

  fun onAddExpenseClicked() {}

  fun onBack() {}

  fun onSettingsClick() {}

  @Serializable data class Config(val householdId: String) : TopLevelDestinationConfig

  interface Factory {
    fun create(cContext: CContext, config: Config): HouseholdDetailsComponent
  }

  data class UiState(
      val household: HouseholdDto? = null,
      // should be out of UiState because loads
      val expenses: List<ExpenseDto> = emptyList(),
      val isLoading: Boolean = false,
      val error: String? = null,
  )
}

class DefaultHouseholdDetailsComponent(
    context: CContext,
    config: HouseholdDetailsComponent.Config,
    private val gateway: HouseholdApi,
    private val expenseApi: ExpenseApi,
) : HouseholdDetailsComponent, CContext by context {

  override val householdId: String = config.householdId
  private val _uiState = MutableStateFlow(HouseholdDetailsComponent.UiState(isLoading = true))
  override val uiState: StateFlow<HouseholdDetailsComponent.UiState> = _uiState

  init {
    doOnCreate { loadDetails() }
  }

  override fun onAddMemberClicked() {
    // TODO: Implement add member
  }

  override fun onAddExpenseClicked() {
    navigation.pushNew(
        AddExpenseComponent.Config(
            householdId = householdId,
            household = uiState.value.household!!,
            me = uiState.value.household!!.members.first(),
        )
    )
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
          val household = gateway.getHousehold(householdId)
          val expenses = expenseApi.getExpenses(householdId)
          _uiState.update { it.copy(household = household, expenses = expenses, isLoading = false) }
        } catch (e: Exception) {
          _uiState.update {
            it.copy(error = e.message ?: "Failed to load household details", isLoading = false)
          }
        }
      }

  class Factory(
      private val gateway: HouseholdApi,
      private val expenseApi: ExpenseApi,
  ) : HouseholdDetailsComponent.Factory {
    override fun create(
        cContext: CContext,
        config: HouseholdDetailsComponent.Config,
    ): HouseholdDetailsComponent =
        DefaultHouseholdDetailsComponent(cContext, config, gateway, expenseApi)
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
