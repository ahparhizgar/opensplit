package com.opensplit.features.expense

import com.ahparhizgar.katch.ApiCallError
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.dto.expense.ParticipantAmount
import com.opensplit.dto.expense.ParticipantShareDto
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.features.household.HouseholdApi
import com.opensplit.remote.fieldErrors
import com.opensplit.root.TopLevelDestinationConfig
import com.opensplit.validation.expense.ExpenseValidation
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface AddExpenseComponent {
  val uiState: Value<AddExpenseUiState>
  val stack: Value<ChildStack<*, Child>>

  fun onTitleChanged(title: String)

  fun onAmountChanged(amount: String)

  fun setPaidAmounts(amounts: PayAmountsUiState)

  fun onParticipantPaidAmountChanged(userId: String, amount: String)

  fun setSplitMethod(method: SplitMethod)

  fun onSaveClicked(): Job

  fun onBackClicked()

  fun onDoneClicked()

  fun navigateToPayerSelection()

  fun navigateToPaidAmounts()

  fun navigateToQuickSplit()

  fun navigateToAdjustSplit()

  @Serializable
  data class Config(
      val householdId: String,
      val household: HouseholdDto,
      val me: HouseholdMemberDto,
  ) : TopLevelDestinationConfig

  sealed class Child {
    class Main(val component: AddExpenseComponent) : Child()

    class PayerSelection(val component: AddExpenseComponent) : Child()

    class PaidAmounts(val component: PaidAmountsComponent) : Child()

    class QuickSplitSelection(val component: AddExpenseComponent) : Child()

    class AdjustSplit(val component: AdjustSplitComponent) : Child()
  }

  interface Factory {
    fun create(
        context: CContext,
        config: Config,
        household: HouseholdDto,
        me: HouseholdMemberDto,
        onFinished: () -> Unit,
    ): AddExpenseComponent
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
    val paidAmount: Double = 0.0,
    val owedAmount: Double = 0.0,
    val isCurrentUser: Boolean = false,
)

sealed interface PayAmountsUiState {
  fun toDomain(): PayAmounts

  data class OnePerson(val userId: String, val amount: String) : PayAmountsUiState {
    override fun toDomain(): PayAmounts = PayAmounts.OnePerson(userId, amount.toDoubleOrNull())
  }

  data class MultiplePeople(val amounts: List<ParticipantValue>) : PayAmountsUiState {
    override fun toDomain(): PayAmounts =
        PayAmounts.MultiplePeople(
            amounts.map { ParticipantAmount(it.userId, it.value.toDoubleOrNull() ?: 0.0) }
        )
  }
}

sealed interface PayAmounts {
  fun sum(): Double

  data class OnePerson(val userId: String, val amount: Double?) : PayAmounts {
    override fun sum(): Double = amount ?: 0.0
  }

  data class MultiplePeople(val amounts: List<ParticipantAmount>) : PayAmounts {
    override fun sum(): Double = amounts.sumOf { it.amount }
  }
}

data class AddExpenseUiState(
    val allParticipants: List<String>,
    val payAmounts: PayAmountsUiState,
    val title: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val splitMethod: SplitMethod = SplitMethod.Equally(emptyList()),
    val isLoading: Boolean = false,
) {
  val payAmountsDomain: PayAmounts = payAmounts.toDomain()
  val amountSum: Double =
      when (payAmountsDomain) {
        is PayAmounts.OnePerson -> payAmountsDomain.amount ?: 0.0
        is PayAmounts.MultiplePeople -> payAmountsDomain.amounts.sumOf { it.amount }
      }
}

class DefaultAddExpenseComponent(
    context: CContext,
    config: AddExpenseComponent.Config,
    private val expenseApi: ExpenseApi,
    private val householdApi: HouseholdApi,
    household: HouseholdDto,
    me: HouseholdMemberDto,
    private val adjustSplitComponentFactory: AdjustSplitComponent.Factory,
    private val onFinished: () -> Unit,
) : AddExpenseComponent, CContext by context {
  private val householdId = config.householdId
  private val _uiState =
      MutableValue(
          AddExpenseUiState(
              allParticipants = household.members.map { it.userId },
              payAmounts = PayAmountsUiState.OnePerson(userId = me.userId, amount = ""),
          )
      )
  override val uiState: Value<AddExpenseUiState> = _uiState
  private val scope = componentScope()

  private val stackNavigation = StackNavigation<AddExpenseChildConfig>()

  override val stack: Value<ChildStack<*, AddExpenseComponent.Child>> =
      childStack(
          source = stackNavigation,
          serializer = AddExpenseChildConfig.serializer(),
          initialConfiguration = AddExpenseChildConfig.Main,
          handleBackButton = true,
          childFactory = { config, componentContext ->
            when (config) {
              is AddExpenseChildConfig.Main -> AddExpenseComponent.Child.Main(this)
              is AddExpenseChildConfig.PayerSelection ->
                  AddExpenseComponent.Child.PayerSelection(this)
              is AddExpenseChildConfig.PaidAmounts ->
                  AddExpenseComponent.Child.PaidAmounts(
                      DefaultPaidAmountsComponent.Factory()
                          .create(
                              initial = _uiState.value.payAmountsDomain,
                              household = household,
                              onDone = { amounts ->
                                setPaidAmounts(amounts)
                                stackNavigation.pop()
                              },
                          )
                  )
              is AddExpenseChildConfig.QuickSplitSelection ->
                  AddExpenseComponent.Child.QuickSplitSelection(this)
              is AddExpenseChildConfig.AdjustSplit ->
                  AddExpenseComponent.Child.AdjustSplit(
                      adjustSplitComponentFactory.create(
                          context = componentContext,
                          initialParticipants = _uiState.value.allParticipants,
                          totalAmount = _uiState.value.payAmountsDomain.sum(),
                          onDone = { splitMethod ->
                            _uiState.update { it.copy(splitMethod = splitMethod) }
                            stackNavigation.navigate { it.dropLast(2) }
                          },
                      )
                  )
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
            ParticipantAmount(
                userId = member.userId,
                amount = 0.0,
            )
          }
      _uiState.update {
        it.copy(
            splitMethod = SplitMethod.Equally(participants.map { p -> p.userId }),
        )
      }
    } finally {
      _uiState.update {
        it.copy(
            isLoading = false,
        )
      }
    }
  }

  override fun onTitleChanged(title: String) {
    _uiState.update { it.copy(title = title, fieldErrors = it.fieldErrors - "title") }
  }

  override fun onAmountChanged(amount: String) {
    _uiState.update { state ->
      state.payAmounts.let {
        when (it) {
          is PayAmountsUiState.MultiplePeople ->
              error("cannot change multiple people amount directly")
          is PayAmountsUiState.OnePerson -> state.copy(payAmounts = it.copy(amount = amount))
        }
      }
    }
  }

  override fun onParticipantPaidAmountChanged(userId: String, amount: String) {
    _uiState.update {
      it.copy(
          payAmounts =
              PayAmountsUiState.MultiplePeople(
                  amounts =
                      it.payAmounts.let { payAmounts ->
                        when (payAmounts) {
                          is PayAmountsUiState.OnePerson ->
                              listOf(
                                  ParticipantValue(
                                      userId = userId,
                                      value = amount,
                                  )
                              )
                          is PayAmountsUiState.MultiplePeople ->
                              payAmounts.amounts.map { participant ->
                                if (participant.userId == userId) {
                                  participant.copy(value = amount)
                                } else {
                                  participant
                                }
                              }
                        }
                      }
              )
      )
    }
  }

  override fun setSplitMethod(method: SplitMethod) {
    _uiState.update { it.copy(splitMethod = method) }
  }

  override fun setPaidAmounts(amounts: PayAmountsUiState) {
    _uiState.update { it.copy(payAmounts = amounts) }
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

  override fun onSaveClicked(): Job = scope.launch {
    val state = _uiState.value
    val title = state.title
    val errors = mutableMapOf<String, String>()
    val amount = state.payAmountsDomain.sum()
    if (amount == 0.0) {
      errors["amount"] = "Invalid amount"
    }

    val validation = ExpenseValidation.validateExpense(title, amount)
    if (!validation.isValid || errors.isNotEmpty()) {
      _uiState.update { it.copy(fieldErrors = it.fieldErrors + validation.errors + errors) }
      return@launch
    }

    val participantsDto =
        state.splitMethod
            .calculateOwedAmounts(
                payAmounts =
                    when (state.payAmountsDomain) {
                      is PayAmounts.OnePerson ->
                          listOf(
                              ParticipantAmount(
                                  userId = state.payAmountsDomain.userId,
                                  amount = state.payAmountsDomain.amount ?: 0.0,
                              )
                          )
                      is PayAmounts.MultiplePeople -> state.payAmountsDomain.amounts
                    },
                allParticipants = state.allParticipants.toSet(),
            )
            .map { it ->
              var paidShare =
                  state.payAmountsDomain.let { payAmounts ->
                    when (payAmounts) {
                      is PayAmounts.OnePerson ->
                          if (payAmounts.userId == it.userId) payAmounts.amount ?: 0.0 else 0.0

                      is PayAmounts.MultiplePeople ->
                          payAmounts.amounts.find { p -> p.userId == it.userId }?.amount ?: 0.0
                    }
                  }
              ParticipantShareDto(
                  userId = it.userId,
                  paidShare = paidShare,
                  owedShare = it.amount,
                  netBalance = paidShare - it.amount,
              )
            }

    _uiState.update { it.copy(isLoading = true) }
    try {
      expenseApi.createExpense(
          householdId,
          title,
          amount,
          participantsDto,
          state.splitMethod,
      )
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

  class Factory(
      private val expenseApi: ExpenseApi,
      private val householdApi: HouseholdApi,
      private val adjustSplitComponentFactory: AdjustSplitComponent.Factory,
  ) : AddExpenseComponent.Factory {
    override fun create(
        context: CContext,
        config: AddExpenseComponent.Config,
        household: HouseholdDto,
        me: HouseholdMemberDto,
        onFinished: () -> Unit,
    ): AddExpenseComponent =
        DefaultAddExpenseComponent(
            context = context,
            config = config,
            expenseApi = expenseApi,
            householdApi = householdApi,
            household = household,
            me = me,
            adjustSplitComponentFactory = adjustSplitComponentFactory,
            onFinished = onFinished,
        )
  }
}

class FakeAddExpenseComponent(
    uiState: AddExpenseUiState =
        AddExpenseUiState(
            allParticipants = listOf("user1"),
            payAmounts = PayAmountsUiState.OnePerson(userId = "user1", amount = ""),
        ),
    childFactory: (AddExpenseComponent) -> AddExpenseComponent.Child = {
      AddExpenseComponent.Child.Main(it)
    },
    adjustSplitComponent: AdjustSplitComponent = FakeAdjustSplitComponent(),
) : AddExpenseComponent {
  override val uiState: Value<AddExpenseUiState> = MutableValue(uiState)
  override val stack: Value<ChildStack<*, AddExpenseComponent.Child>> =
      MutableValue(
          ChildStack(
              configuration = Unit,
              instance = childFactory(this),
          )
      )

  override fun onTitleChanged(title: String) {}

  override fun onAmountChanged(amount: String) {}

  override fun setPaidAmounts(amounts: PayAmountsUiState) {
    TODO("Not yet implemented")
  }

  override fun onParticipantPaidAmountChanged(userId: String, amount: String) {}

  override fun setSplitMethod(method: SplitMethod) {
    TODO("Not yet implemented")
  }

  override fun onSaveClicked(): Job = Job()

  override fun onBackClicked() {}

  override fun onDoneClicked() {}

  override fun navigateToPayerSelection() {}

  override fun navigateToPaidAmounts() {}

  override fun navigateToQuickSplit() {}

  override fun navigateToAdjustSplit() {}
}
