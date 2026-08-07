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
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.Household
import com.opensplit.domain.Member
import com.opensplit.domain.ParticipantShare
import com.opensplit.dto.expense.ParticipantAmount
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.remote.fieldErrors
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.HouseholdRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.root.TopLevelDestinationConfig
import com.opensplit.util.formatAmount
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
  ) : TopLevelDestinationConfig

  sealed class Child {
    class Main(val component: AddExpenseComponent) : Child()

    class WhoPaid(val component: WhoPaidComponent) : Child()

    class PaidAmounts(val component: PaidAmountsComponent) : Child()

    class QuickSplitSelection(val component: QuickSplitComponent) : Child()

    class MoreSplitOptions(val component: MoreSplitOptionsComponent) : Child()
  }

  interface Factory {
    fun create(
        context: CContext,
        config: Config,
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

  @Serializable data object MoreSplitOptions : AddExpenseChildConfig()
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
    val householdName: String = "",
    val allParticipants: List<String>,
    val participants: List<Member> = emptyList(),
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

  val summaryText: String?
    get() {
      if (participants.size != 2 || amountSum <= 0.0) return null
      val other = participants.find { !it.isCurrentUser } ?: return null
      val youId = participants.find { it.isCurrentUser }?.userId ?: return null

      val option =
          QuickSplitComponent.getOption(
              payAmounts = payAmounts,
              splitMethod = splitMethod,
              youId = youId,
              otherId = other.userId,
              amountSum = amountSum,
              allParticipants = allParticipants,
          )

      return when (option) {
        null -> null
        QuickSplitComponent.QuickSplitOption.YOU_PAID_SPLIT_EQUALLY ->
            "${other.name} owes you IRR ${(amountSum / 2).formatAmount()}"
        QuickSplitComponent.QuickSplitOption.YOU_ARE_OWED_FULL_AMOUNT ->
            "${other.name} owes you IRR ${amountSum.formatAmount()}"
        QuickSplitComponent.QuickSplitOption.OTHER_PAID_SPLIT_EQUALLY ->
            "You owe ${other.name} IRR ${(amountSum / 2).formatAmount()}"
        QuickSplitComponent.QuickSplitOption.OTHER_IS_OWED_FULL_AMOUNT ->
            "You owe ${other.name} IRR ${amountSum.formatAmount()}"
      }
    }

  fun getParticipantName(userId: String): String {
    val member = participants.find { it.userId == userId }
    return when {
      member?.isCurrentUser == true -> "you"
      member != null -> member.name
      else -> userId
    }
  }
}

class DefaultAddExpenseComponent(
    context: CContext,
    config: AddExpenseComponent.Config,
    private val expenseRepository: ExpenseRepository,
    private val householdRepository: HouseholdRepository,
    private val profileRepository: ProfileRepository,
    private val moreSplitOptionsComponentFactory: MoreSplitOptionsComponent.Factory,
    private val whoPaidComponentFactory: WhoPaidComponent.Factory,
    private val quickSplitComponentFactory: QuickSplitComponent.Factory,
    private val onFinished: () -> Unit,
) : AddExpenseComponent, CContext by context {
  private val householdId = config.householdId
  private var loadedHousehold: Household? = null
  private val _uiState =
      MutableValue(
          AddExpenseUiState(
              allParticipants = emptyList(),
              participants = emptyList(),
              payAmounts = PayAmountsUiState.OnePerson(userId = "", amount = ""),
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
                  AddExpenseComponent.Child.WhoPaid(
                      whoPaidComponentFactory.create(
                          context = componentContext,
                          participants = _uiState.value.participants,
                          selectedUserId =
                              (_uiState.value.payAmounts as? PayAmountsUiState.OnePerson)?.userId,
                          onParticipantSelected = { userId ->
                            val currentAmount =
                                (_uiState.value.payAmounts as? PayAmountsUiState.OnePerson)?.amount
                                    ?: ""
                            setPaidAmounts(PayAmountsUiState.OnePerson(userId, currentAmount))
                            stackNavigation.pop()
                          },
                          onMultiplePeopleClicked = { navigateToPaidAmounts() },
                      )
                  )
              is AddExpenseChildConfig.PaidAmounts ->
                  AddExpenseComponent.Child.PaidAmounts(
                      DefaultPaidAmountsComponent.Factory()
                          .create(
                              initial = _uiState.value.payAmountsDomain,
                              household = loadedHousehold!!,
                              onDone = { amounts ->
                                setPaidAmounts(amounts)
                                stackNavigation.pop()
                              },
                          )
                  )
              is AddExpenseChildConfig.QuickSplitSelection -> {
                val state = _uiState.value
                val currentUserId = profileRepository.profile.value?.id ?: ""
                val otherId =
                    state.participants.firstOrNull { it.userId != currentUserId }?.userId ?: ""
                val amountText =
                    when (val p = state.payAmounts) {
                      is PayAmountsUiState.OnePerson -> p.amount
                      is PayAmountsUiState.MultiplePeople -> state.amountSum.toString()
                    }
                AddExpenseComponent.Child.QuickSplitSelection(
                    quickSplitComponentFactory.create(
                        context = componentContext,
                        allParticipants = state.allParticipants,
                        amountText = amountText,
                        amountSum = state.amountSum,
                        householdId = householdId,
                        initialOption =
                            QuickSplitComponent.getOption(
                                payAmounts = state.payAmounts,
                                splitMethod = state.splitMethod,
                                youId = currentUserId,
                                otherId = otherId,
                                amountSum = state.amountSum,
                                allParticipants = state.allParticipants,
                            ),
                        onOptionSelected = { amounts, method ->
                          setPaidAmounts(amounts)
                          setSplitMethod(method)
                          stackNavigation.pop()
                        },
                        onAdjustSplitClicked = { navigateToAdjustSplit() },
                    )
                )
              }
              is AddExpenseChildConfig.MoreSplitOptions ->
                  AddExpenseComponent.Child.MoreSplitOptions(
                      moreSplitOptionsComponentFactory.create(
                          context = componentContext,
                          participants = _uiState.value.participants,
                          totalAmount = _uiState.value.payAmountsDomain.sum(),
                          initialSplitMethod = _uiState.value.splitMethod,
                          payerName =
                              _uiState.map { state ->
                                when (state.payAmountsDomain) {
                                  is PayAmounts.MultiplePeople -> "Multiple people"
                                  is PayAmounts.OnePerson ->
                                      state.getParticipantName(state.payAmountsDomain.userId)
                                }
                              },
                          onPayerClicked = { navigateToPayerSelection() },
                          onDone = { splitMethod ->
                            _uiState.update { it.copy(splitMethod = splitMethod) }
                            stackNavigation.navigate {
                              it.filterNot { c -> c is AddExpenseChildConfig.QuickSplitSelection }
                                  .dropLast(1)
                            }
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
      val household = householdRepository.getHousehold(householdId)
      if (household != null) {
        loadedHousehold = household
        val currentUserId = profileRepository.profile.value?.id
        val participants =
            household.members.map { member ->
              ParticipantAmount(userId = member.userId, amount = 0.0)
            }
        _uiState.update {
          it.copy(
              householdName = household.name,
              allParticipants = household.members.map { it.userId },
              participants = household.members,
              payAmounts =
                  if (
                      it.payAmounts is PayAmountsUiState.OnePerson && it.payAmounts.userId.isEmpty()
                  )
                      PayAmountsUiState.OnePerson(
                          userId = currentUserId ?: household.members.first().userId,
                          amount = it.payAmounts.amount,
                      )
                  else it.payAmounts,
              splitMethod = SplitMethod.Equally(participants.map { p -> p.userId }),
          )
        }
      }
    } finally {
      _uiState.update { it.copy(isLoading = false) }
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
                                      name = it.getParticipantName(userId),
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
    stackNavigation.pushNew(AddExpenseChildConfig.MoreSplitOptions)
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

    val participantsDomain =
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
            .map {
              val paidShare =
                  state.payAmountsDomain.let { payAmounts ->
                    when (payAmounts) {
                      is PayAmounts.OnePerson ->
                          if (payAmounts.userId == it.userId) payAmounts.amount ?: 0.0 else 0.0
                      is PayAmounts.MultiplePeople ->
                          payAmounts.amounts.find { p -> p.userId == it.userId }?.amount ?: 0.0
                    }
                  }
              ParticipantShare(
                  userId = it.userId,
                  paidShare = paidShare,
                  owedShare = paidShare - it.amount,
                  netBalance = it.amount,
              )
            }

    _uiState.update { it.copy(isLoading = true) }
    try {
      expenseRepository.createExpense(
          householdId = householdId,
          title = title,
          amount = amount,
          payerId = participantsDomain.firstOrNull { it.paidShare > 0 }?.userId ?: "",
          participants = participantsDomain,
          splitMethod = state.splitMethod,
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
      private val expenseRepository: ExpenseRepository,
      private val householdRepository: HouseholdRepository,
      private val profileRepository: ProfileRepository,
      private val moreSplitOptionsComponentFactory: MoreSplitOptionsComponent.Factory,
      private val whoPaidComponentFactory: WhoPaidComponent.Factory,
      private val quickSplitComponentFactory: QuickSplitComponent.Factory,
  ) : AddExpenseComponent.Factory {
    override fun create(
        context: CContext,
        config: AddExpenseComponent.Config,
        onFinished: () -> Unit,
    ): AddExpenseComponent =
        DefaultAddExpenseComponent(
            context = context,
            config = config,
            expenseRepository = expenseRepository,
            householdRepository = householdRepository,
            profileRepository = profileRepository,
            moreSplitOptionsComponentFactory = moreSplitOptionsComponentFactory,
            whoPaidComponentFactory = whoPaidComponentFactory,
            quickSplitComponentFactory = quickSplitComponentFactory,
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
    moreSplitOptionsComponent: MoreSplitOptionsComponent = FakeMoreSplitOptionsComponent(),
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
