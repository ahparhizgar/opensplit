package com.opensplit.features.expense

import com.ahparhizgar.katch.ApiCallError
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.remote.fieldErrors
import com.opensplit.validation.expense.ExpenseValidation
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface AddExpenseComponent {
  val uiState: Value<AddExpenseUiState>

  fun onTitleChanged(title: String)

  fun onAmountChanged(amount: String)

  fun onSaveClicked(): Job

  fun onBackClicked()

  @Serializable
  data class Config(val householdId: String) : com.opensplit.root.TopLevelDestinationConfig

  interface Factory {
    fun create(context: CContext, config: Config, onFinished: () -> Unit): AddExpenseComponent
  }
}

data class AddExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
)

class DefaultAddExpenseComponent(
    context: CContext,
    config: AddExpenseComponent.Config,
    private val expenseApi: ExpenseApi,
    private val onFinished: () -> Unit,
) : AddExpenseComponent, CContext by context {
  private val householdId = config.householdId
  private val _uiState = MutableValue(AddExpenseUiState())
  override val uiState: Value<AddExpenseUiState> = _uiState
  private val scope = componentScope()

  override fun onTitleChanged(title: String) {
    _uiState.update { it.copy(title = title, fieldErrors = it.fieldErrors - "title") }
  }

  override fun onAmountChanged(amount: String) {
    _uiState.update { it.copy(amount = amount, fieldErrors = it.fieldErrors - "amount") }
  }

  override fun onSaveClicked(): Job {
    val title = _uiState.value.title
    val amountStr = _uiState.value.amount
    val amount = amountStr.toDoubleOrNull() ?: 0.0

    val validation = ExpenseValidation.validateExpense(title, amount)
    if (!validation.isValid) {
      _uiState.update { it.copy(fieldErrors = validation.errors) }
      return Job().apply { complete() }
    }

    _uiState.update { it.copy(isLoading = true) }
    return scope.launch {
      try {
        expenseApi.createExpense(householdId, title, amount)
        onFinished()
      } catch (e: ApiCallError) {
        _uiState.update { it.copy(fieldErrors = e.fieldErrors) }
      } finally {
        _uiState.update { it.copy(isLoading = false) }
      }
    }
  }

  override fun onBackClicked() {
    onFinished()
  }

  class Factory(private val expenseApi: ExpenseApi) : AddExpenseComponent.Factory {
    override fun create(
        context: CContext,
        config: AddExpenseComponent.Config,
        onFinished: () -> Unit,
    ): AddExpenseComponent = DefaultAddExpenseComponent(context, config, expenseApi, onFinished)
  }
}

class FakeAddExpenseComponent(uiState: AddExpenseUiState = AddExpenseUiState()) :
    AddExpenseComponent {
  override val uiState: Value<AddExpenseUiState> = MutableValue(uiState)

  override fun onTitleChanged(title: String) {}

  override fun onAmountChanged(amount: String) {}

  override fun onSaveClicked(): Job = Job()

  override fun onBackClicked() {}
}
