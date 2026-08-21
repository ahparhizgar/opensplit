package com.opensplit.features.expense

import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.Expense
import com.opensplit.domain.Member
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.HouseholdRepository
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface ExpenseDetailsComponent {
  val uiState: Value<ExpenseDetailsUiState>

  fun onBackClicked()

  fun onDeleteClicked()

  fun onEditClicked()

  fun onAddReceiptClicked()

  @Serializable
  data class Config(val householdId: String, val expenseId: String) : TopLevelDestinationConfig

  interface Factory {
    fun create(
        context: CContext,
        config: Config,
        onBack: () -> Unit,
    ): ExpenseDetailsComponent
  }
}

data class ExpenseDetailsUiState(
    val expense: Expense? = null,
    val householdMembers: List<Member> = emptyList(),
    val isLoading: Boolean = false,
)

class DefaultExpenseDetailsComponent(
    context: CContext,
    private val config: ExpenseDetailsComponent.Config,
    private val expenseRepository: ExpenseRepository,
    private val householdRepository: HouseholdRepository,
    private val onBack: () -> Unit,
) : ExpenseDetailsComponent, CContext by context {

  private val _uiState = MutableValue(ExpenseDetailsUiState())
  override val uiState: Value<ExpenseDetailsUiState> = _uiState
  private val scope = componentScope()

  init {
    loadData()
  }

  private fun loadData() = scope.launch {
    _uiState.update { it.copy(isLoading = true) }

    launch {
      householdRepository.observeHousehold(config.householdId).collectLatest { household ->
        _uiState.update { it.copy(householdMembers = household?.members ?: emptyList()) }
      }
    }

    expenseRepository.getExpense(config.expenseId).collectLatest { expense ->
      _uiState.update { it.copy(expense = expense, isLoading = false) }
    }
  }

  override fun onBackClicked() {
    onBack()
  }

  override fun onDeleteClicked() {
    scope.launch {
      expenseRepository.deleteExpense(config.householdId, config.expenseId)
      onBack()
    }
  }

  override fun onEditClicked() {
    navigation.pushNew(
        AddExpenseComponent.Config(
            householdId = config.householdId,
            expenseId = config.expenseId,
        )
    )
  }

  override fun onAddReceiptClicked() {
    // TODO: implement add receipt
  }

  class Factory(
      private val expenseRepository: ExpenseRepository,
      private val householdRepository: HouseholdRepository,
  ) : ExpenseDetailsComponent.Factory {
    override fun create(
        context: CContext,
        config: ExpenseDetailsComponent.Config,
        onBack: () -> Unit,
    ): ExpenseDetailsComponent =
        DefaultExpenseDetailsComponent(
            context = context,
            config = config,
            expenseRepository = expenseRepository,
            householdRepository = householdRepository,
            onBack = onBack,
        )
  }
}

class FakeExpenseDetailsComponent(uiState: ExpenseDetailsUiState = ExpenseDetailsUiState()) :
    ExpenseDetailsComponent {
  override val uiState: Value<ExpenseDetailsUiState> = MutableValue(uiState)

  override fun onBackClicked() {}

  override fun onDeleteClicked() {}

  override fun onEditClicked() {}

  override fun onAddReceiptClicked() {}
}
