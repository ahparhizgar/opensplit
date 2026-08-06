package com.opensplit.features.household.details

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.Expense
import com.opensplit.domain.Household
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.household.settings.HouseholdSettingsComponent
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.HouseholdRepository
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
      val household: Household? = null,
      val expenses: List<Expense> = emptyList(),
      val error: String? = null,
  )
}

class DefaultHouseholdDetailsComponent(
    context: CContext,
    config: HouseholdDetailsComponent.Config,
    private val householdRepository: HouseholdRepository,
    private val expenseRepository: ExpenseRepository,
) : HouseholdDetailsComponent, CContext by context {

  override val householdId: String = config.householdId
  private val _uiState = MutableStateFlow(HouseholdDetailsComponent.UiState())
  override val uiState: StateFlow<HouseholdDetailsComponent.UiState> = _uiState

  init {
    componentScope().launch {
      householdRepository.observeHousehold(householdId).collect { household ->
        _uiState.update { it.copy(household = household) }
      }
    }
    componentScope().launch {
      expenseRepository.getExpenses(householdId).collect { expenses ->
        _uiState.update { it.copy(expenses = expenses) }
      }
    }
  }

  override fun onAddMemberClicked() {
    // TODO: Implement add member
  }

  override fun onAddExpenseClicked() {
    navigation.pushNew(AddExpenseComponent.Config(householdId = householdId))
  }

  override fun onSettingsClick() {
    navigation.pushNew(HouseholdSettingsComponent.Config(householdId))
  }

  override fun onBack() {
    navigation.pop()
  }

  class Factory(
      private val householdRepository: HouseholdRepository,
      private val expenseRepository: ExpenseRepository,
  ) : HouseholdDetailsComponent.Factory {
    override fun create(
        cContext: CContext,
        config: HouseholdDetailsComponent.Config,
    ): HouseholdDetailsComponent =
        DefaultHouseholdDetailsComponent(cContext, config, householdRepository, expenseRepository)
  }
}

class FakeHouseholdDetailsComponent(
    override val householdId: String = "h12345",
    uiState: HouseholdDetailsComponent.UiState = HouseholdDetailsComponent.UiState(),
) : HouseholdDetailsComponent {
  override val uiState: StateFlow<HouseholdDetailsComponent.UiState> = MutableStateFlow(uiState)
}
