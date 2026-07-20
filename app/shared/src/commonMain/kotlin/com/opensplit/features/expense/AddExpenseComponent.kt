package com.opensplit.features.expense

import com.ahparhizgar.katch.ApiCallError
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.expense.SplitCalculator
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitType
import com.opensplit.features.household.HouseholdApi
import com.opensplit.remote.fieldErrors
import com.opensplit.validation.expense.ExpenseValidation
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface AddExpenseComponent {
  val uiState: Value<AddExpenseUiState>
  val stack: Value<ChildStack<*, Child>>

  fun onTitleChanged(title: String)

  fun onAmountChanged(amount: String)

  fun onSplitTypeChanged(splitType: SplitType)

  fun onParticipantOwedAmountChanged(userId: String, amount: String)

  fun onParticipantPercentageChanged(userId: String, percentage: String)

  fun onParticipantSharesChanged(userId: String, shares: String)

  fun onParticipantAdjustmentChanged(userId: String, adjustment: String)

  fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean)

  fun onParticipantPaidAmountChanged(userId: String, amount: String)

  fun onSaveClicked(): Job

  fun onBackClicked()

  fun onDoneClicked()

  fun navigateToPayerSelection()

  fun navigateToPaidAmounts()

  fun navigateToQuickSplit()

  fun navigateToAdjustSplit()

  @Serializable
  data class Config(val householdId: String) : com.opensplit.root.TopLevelDestinationConfig

  sealed class Child {
    class Main(val component: AddExpenseComponent) : Child()

    class PayerSelection(val component: AddExpenseComponent) : Child()

    class PaidAmounts(val component: AddExpenseComponent) : Child()

    class QuickSplitSelection(val component: AddExpenseComponent) : Child()

    class AdjustSplit(val component: AddExpenseComponent) : Child()
  }

  interface Factory {
    fun create(context: CContext, config: Config, onFinished: () -> Unit): AddExpenseComponent
  }
}

@Serializable
sealed class AddExpenseChildConfig {
  @Serializable data object Main : AddExpenseChildConfig()

  @Serializable data object PayerSelection : AddExpenseChildConfig()

  @Serializable data object PaidAmounts : AddExpenseChildConfig()

  @Serializable data object QuickSplitSelection : AddExpenseChildConfig()

  @Serializable data object AdjustSplit : AddExpenseChildConfig()
}

data class ParticipantState(
    val userId: String,
    val name: String,
    val paidAmount: String = "0",
    val owedAmount: String = "0",
    val percentage: String = "0",
    val shares: String = "1",
    val adjustment: String = "0",
    val isIncluded: Boolean = true,
    val isCurrentUser: Boolean = false,
)

data class AddExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val splitType: SplitType = SplitType.EQUALLY,
    val participants: List<ParticipantState> = emptyList(),
)

class DefaultAddExpenseComponent(
    context: CContext,
    config: AddExpenseComponent.Config,
    private val expenseApi: ExpenseApi,
    private val householdApi: HouseholdApi,
    private val onFinished: () -> Unit,
) : AddExpenseComponent, CContext by context {
  private val householdId = config.householdId
  private val _uiState = MutableValue(AddExpenseUiState())
  override val uiState: Value<AddExpenseUiState> = _uiState
  private val scope = componentScope()

  private val stackNavigation = StackNavigation<AddExpenseChildConfig>()

  override val stack: Value<ChildStack<*, AddExpenseComponent.Child>> =
      childStack(
          source = stackNavigation,
          serializer = AddExpenseChildConfig.serializer(),
          initialConfiguration = AddExpenseChildConfig.Main,
          handleBackButton = true,
          childFactory = { config, _ ->
            when (config) {
              is AddExpenseChildConfig.Main -> AddExpenseComponent.Child.Main(this)
              is AddExpenseChildConfig.PayerSelection ->
                  AddExpenseComponent.Child.PayerSelection(this)
              is AddExpenseChildConfig.PaidAmounts -> AddExpenseComponent.Child.PaidAmounts(this)
              is AddExpenseChildConfig.QuickSplitSelection ->
                  AddExpenseComponent.Child.QuickSplitSelection(this)
              is AddExpenseChildConfig.AdjustSplit -> AddExpenseComponent.Child.AdjustSplit(this)
            }
          },
      )

  init {
    loadMembers()
  }

  private fun loadMembers() = scope.launch {
    _uiState.update { it.copy(isLoading = true) }
    try {
      val household = householdApi.getHousehold(householdId)
      val participants =
          household.members.map { member ->
            ParticipantState(
                userId = member.userId,
                name = member.name ?: member.email,
                paidAmount = "0",
                isIncluded = true,
                isCurrentUser = member.isCurrentUser,
            )
          }
      _uiState.update { it.copy(participants = participants) }

      recalculate()
    } catch (ignore: Exception) {} finally {
      _uiState.update { it.copy(isLoading = false) }
    }
  }

  override fun onTitleChanged(title: String) {
    _uiState.update { it.copy(title = title, fieldErrors = it.fieldErrors - "title") }
  }

  override fun onAmountChanged(amount: String) {
    _uiState.update { state ->
      val paidParticipants =
          state.participants.filter { (it.paidAmount.toDoubleOrNull() ?: 0.0) > 0.0 }
      val updatedParticipants =
          if (paidParticipants.size <= 1) {
            state.participants.map {
              val shouldUpdate =
                  if (paidParticipants.isEmpty()) it.isCurrentUser
                  else it.userId == paidParticipants.first().userId
              if (shouldUpdate) {
                it.copy(paidAmount = amount)
              } else it
            }
          } else state.participants

      state.copy(
          amount = amount,
          participants = updatedParticipants,
          fieldErrors = state.fieldErrors - "amount",
      )
    }
    recalculate()
  }

  override fun onSplitTypeChanged(splitType: SplitType) {
    _uiState.update { it.copy(splitType = splitType) }
    recalculate()
  }

  override fun onParticipantOwedAmountChanged(userId: String, amount: String) {
    updateParticipant(userId) { it.copy(owedAmount = amount) }
    recalculate()
  }

  override fun onParticipantPercentageChanged(userId: String, percentage: String) {
    updateParticipant(userId) { it.copy(percentage = percentage) }
    recalculate()
  }

  override fun onParticipantSharesChanged(userId: String, shares: String) {
    updateParticipant(userId) { it.copy(shares = shares) }
    recalculate()
  }

  override fun onParticipantAdjustmentChanged(userId: String, adjustment: String) {
    updateParticipant(userId) { it.copy(adjustment = adjustment) }
    recalculate()
  }

  override fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean) {
    updateParticipant(userId) { it.copy(isIncluded = isIncluded) }
    recalculate()
  }

  override fun onParticipantPaidAmountChanged(userId: String, amount: String) {
    updateParticipant(userId) { it.copy(paidAmount = amount) }
  }

  override fun navigateToPayerSelection() {
    stackNavigation.pushNew(AddExpenseChildConfig.PayerSelection)
  }

  override fun navigateToPaidAmounts() {
    stackNavigation.pushNew(AddExpenseChildConfig.PaidAmounts)
  }

  override fun navigateToQuickSplit() {
    stackNavigation.pushNew(AddExpenseChildConfig.QuickSplitSelection)
  }

  override fun navigateToAdjustSplit() {
    stackNavigation.pushNew(AddExpenseChildConfig.AdjustSplit)
  }

  override fun onDoneClicked() {
    stackNavigation.popTo(0)
  }

  private fun updateParticipant(userId: String, block: (ParticipantState) -> ParticipantState) {
    _uiState.update { state ->
      state.copy(
          participants = state.participants.map { if (it.userId == userId) block(it) else it }
      )
    }
  }

  private fun recalculate() {
    val state = _uiState.value
    val amount = state.amount.toDoubleOrNull() ?: 0.0
    val includedParticipants = state.participants.filter { it.isIncluded }

    val owedAmounts =
        when (state.splitType) {
          SplitType.EQUALLY ->
              SplitCalculator.calculateEqual(amount, includedParticipants.map { it.userId })
          SplitType.EXACT ->
              SplitCalculator.calculateExact(
                  state.participants.associate {
                    it.userId to (it.owedAmount.toDoubleOrNull() ?: 0.0)
                  }
              )
          SplitType.PERCENTAGE ->
              SplitCalculator.calculatePercentage(
                  amount,
                  state.participants.associate {
                    it.userId to (it.percentage.toDoubleOrNull() ?: 0.0)
                  },
              )
          SplitType.SHARES ->
              SplitCalculator.calculateShares(
                  amount,
                  state.participants.associate { it.userId to (it.shares.toDoubleOrNull() ?: 0.0) },
              )
          SplitType.ADJUSTMENT ->
              SplitCalculator.calculateAdjustment(
                  amount,
                  state.participants.associate {
                    it.userId to (it.adjustment.toDoubleOrNull() ?: 0.0)
                  },
                  includedParticipants.map { it.userId },
              )
        }

    _uiState.update { currentState ->
      currentState.copy(
          participants =
              currentState.participants.map { p ->
                p.copy(owedAmount = (owedAmounts[p.userId] ?: 0.0).toString())
              }
      )
    }
  }

  override fun onSaveClicked(): Job = scope.launch {
    val state = _uiState.value
    val title = state.title
    val amountStr = state.amount
    val amount = amountStr.toDoubleOrNull()

    val errors = mutableMapOf<String, String>()
    if (amount == null) {
      errors["amount"] = "Invalid amount"
    }

    val validation = ExpenseValidation.validateExpense(title, amount ?: 0.0)
    if (!validation.isValid || errors.isNotEmpty()) {
      _uiState.update { it.copy(fieldErrors = it.fieldErrors + validation.errors + errors) }
      return@launch
    }

    val participantsDto =
        state.participants.map {
          val paid = it.paidAmount.toDoubleOrNull() ?: 0.0
          val owed = it.owedAmount.toDoubleOrNull() ?: 0.0
          ParticipantShareDto(
              userId = it.userId,
              paidShare = paid,
              owedShare = owed,
              netBalance = paid - owed,
          )
        }

    val totalPaid = participantsDto.sumOf { it.paidShare }
    val totalOwed = participantsDto.sumOf { it.owedShare }

    if (kotlin.math.abs(totalPaid - (amount ?: 0.0)) > 0.01) {
      _uiState.update {
        it.copy(
            fieldErrors =
                it.fieldErrors +
                    ("amount" to "Total paid ($totalPaid) does not match cost ($amount)")
        )
      }
      return@launch
    }

    if (kotlin.math.abs(totalOwed - (amount ?: 0.0)) > 0.01) {
      _uiState.update {
        it.copy(
            fieldErrors =
                it.fieldErrors +
                    ("amount" to "Total owed ($totalOwed) does not match cost ($amount)")
        )
      }
      return@launch
    }

    _uiState.update { it.copy(isLoading = true) }
    try {
      expenseApi.createExpense(householdId, title, amount ?: 0.0, participantsDto)
      onFinished()
    } catch (e: ApiCallError) {
      _uiState.update { it.copy(fieldErrors = e.fieldErrors) }
    } finally {
      _uiState.update { it.copy(isLoading = false) }
    }
  }

  override fun onBackClicked() {
    if (stack.value.items.size > 1) {
      stackNavigation.pop()
    } else {
      onFinished()
    }
  }

  class Factory(private val expenseApi: ExpenseApi, private val householdApi: HouseholdApi) :
      AddExpenseComponent.Factory {
    override fun create(
        context: CContext,
        config: AddExpenseComponent.Config,
        onFinished: () -> Unit,
    ): AddExpenseComponent =
        DefaultAddExpenseComponent(context, config, expenseApi, householdApi, onFinished)
  }
}

class FakeAddExpenseComponent(uiState: AddExpenseUiState = AddExpenseUiState()) :
    AddExpenseComponent {
  override val uiState: Value<AddExpenseUiState> = MutableValue(uiState)
  override val stack: Value<ChildStack<*, AddExpenseComponent.Child>> =
      MutableValue(
          ChildStack(
              configuration = Unit,
              instance = AddExpenseComponent.Child.Main(this),
          )
      )

  override fun onTitleChanged(title: String) {}

  override fun onAmountChanged(amount: String) {}

  override fun onSplitTypeChanged(splitType: SplitType) {}

  override fun onParticipantOwedAmountChanged(userId: String, amount: String) {}

  override fun onParticipantPercentageChanged(userId: String, percentage: String) {}

  override fun onParticipantSharesChanged(userId: String, shares: String) {}

  override fun onParticipantAdjustmentChanged(userId: String, adjustment: String) {}

  override fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean) {}

  override fun onParticipantPaidAmountChanged(userId: String, amount: String) {}

  override fun onSaveClicked(): Job = Job()

  override fun onBackClicked() {}

  override fun onDoneClicked() {}

  override fun navigateToPayerSelection() {}

  override fun navigateToPaidAmounts() {}

  override fun navigateToQuickSplit() {}

  override fun navigateToAdjustSplit() {}
}
