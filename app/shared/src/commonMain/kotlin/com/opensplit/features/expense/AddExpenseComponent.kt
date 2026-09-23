package com.opensplit.features.expense

import com.ahparhizgar.katch.ApiCallError
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.Group
import com.opensplit.domain.Member
import com.opensplit.domain.ParticipantShare
import com.opensplit.dto.expense.ParticipantAmount
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.remote.fieldErrors
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.GroupRepository
import com.opensplit.repository.ProfileRepository
import com.opensplit.util.formatAmount
import com.opensplit.validation.expense.ExpenseValidation
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.abs

interface AddExpenseComponent {
  val uiState: Value<AddExpenseUiState>

  fun onTitleChanged(title: String)

  fun onAmountChanged(amount: String)

  fun setPaidAmounts(amounts: PayAmountsUiState)

  fun onParticipantPaidAmountChanged(userId: String, amount: String)

  fun setSplitMethod(method: SplitMethod)

  fun onSaveClicked(): Job

  fun onBackClicked()

  fun navigateToPayerSelection()

  fun navigateToQuickSplit()

  @Serializable
  data class Config(
      val groupId: String,
      val expenseId: String? = null,
  )
}

interface AddExpenseComponentFactory {
  fun create(
      context: CContext,
      groupId: String,
      expenseId: String?,
      onNavigateToPayerFlow: () -> Unit,
      onNavigateToSplitFlow: () -> Unit,
      onFinished: () -> Unit,
  ): AddExpenseComponent
}

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
    val groupName: String = "",
    val allParticipants: List<String>,
    val participants: List<Member> = emptyList(),
    val payAmounts: PayAmountsUiState,
    val title: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val splitMethod: SplitMethod = SplitMethod.Equally(emptyList()),
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
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
    private val groupId: String,
    private val expenseId: String?,
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val profileRepository: ProfileRepository,
    private val onNavigateToPayerFlow: () -> Unit,
    private val onNavigateToSplitFlow: () -> Unit,
    private val onFinished: () -> Unit,
) : AddExpenseComponent, CContext by context {
  private var loadedGroup: Group? = null
  private val _uiState =
      MutableValue(
          AddExpenseUiState(
              allParticipants = emptyList(),
              participants = emptyList(),
              payAmounts = PayAmountsUiState.OnePerson(userId = "", amount = ""),
              isEditMode = expenseId != null,
          )
      )
  override val uiState: Value<AddExpenseUiState> = _uiState
  private val scope = componentScope()

  init {
    loadMembers()
    if (expenseId != null) {
      loadExpenseForEdit(expenseId)
    }
  }

  private fun loadExpenseForEdit(expenseId: String) = scope.launch {
    expenseRepository.getExpense(expenseId).collect { expense ->
      if (expense != null) {
        _uiState.update { state ->
          val payers = expense.participants.filter { it.paidShare > 0 }
          state.copy(
              title = expense.title,
              payAmounts =
                  if (payers.size == 1) {
                    PayAmountsUiState.OnePerson(
                        userId = payers.first().userId,
                        amount = expense.amount.toString(),
                    )
                  } else {
                    PayAmountsUiState.MultiplePeople(
                        amounts =
                            payers.map {
                              ParticipantValue(
                                  userId = it.userId,
                                  name = state.getParticipantName(it.userId),
                                  value = it.paidShare.toString(),
                              )
                            }
                    )
                  },
              splitMethod = expense.splitMethod,
          )
        }
      }
    }
  }

  private fun loadMembers() = scope.launch {
    _uiState.update { it.copy(isLoading = true) }
    try {
      val group = groupRepository.getGroup(groupId)
      if (group != null) {
        loadedGroup = group
        val currentUserId = profileRepository.profile.value?.id
        val participants =
            group.members.map { member -> ParticipantAmount(userId = member.userId, amount = 0.0) }
        _uiState.update { state ->
          state.copy(
              groupName = group.name,
              allParticipants = group.members.map { it.userId },
              participants = group.members,
              payAmounts =
                  if (
                      state.payAmounts is PayAmountsUiState.OnePerson &&
                          state.payAmounts.userId.isEmpty()
                  )
                      PayAmountsUiState.OnePerson(
                          userId = currentUserId ?: group.members.first().userId,
                          amount = state.payAmounts.amount,
                      )
                  else state.payAmounts,
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
          is PayAmountsUiState.OnePerson -> {

            state.copy(
                payAmounts = it.copy(amount = amount),
                fieldErrors = state.fieldErrors - "amount",
            )
          }
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
    onNavigateToPayerFlow()
  }

  override fun navigateToQuickSplit() {
    onNavigateToSplitFlow()
  }

  override fun onSaveClicked(): Job = scope.launch {
    val state = _uiState.value
    val title = state.title
    val errors = mutableMapOf<String, String>()
    val amount = state.payAmountsDomain.sum()
    if (amount == 0.0) {
      errors["amount"] = "Invalid amount"
    }

    if (state.splitMethod is SplitMethod.Unequally) {
      val splitSum = state.splitMethod.amounts.values.sum()
      if (abs(splitSum - amount) > 0.001) {
        errors["amount"] =
            "Total: IRR ${amount.formatAmount()}, Split: IRR ${splitSum.formatAmount()}"
      }
    }

    val validation = ExpenseValidation.validateExpense(title, amount)
    if (!validation.isValid || errors.isNotEmpty()) {
      _uiState.update { it.copy(fieldErrors = it.fieldErrors + validation.errors + errors) }
      return@launch
    }

    val participantsDomain =
        state.splitMethod
            .calculateConsumedAmounts(
                totalAmount = amount,
                allMembers = state.allParticipants.toSet(),
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
                  consumedShare = it.amount,
              )
            }

    _uiState.update { it.copy(isLoading = true) }
    try {
      if (expenseId != null) {
        // Update existing expense
        expenseRepository.updateExpense(
            groupId = groupId,
            expenseId = expenseId,
            title = title,
            amount = amount,
            creator = participantsDomain.firstOrNull { it.paidShare > 0 }?.userId ?: "",
            shares = participantsDomain,
            splitMethod = state.splitMethod,
        )
      } else {
        // Create new expense
        expenseRepository.createExpense(
            groupId = groupId,
            title = title,
            amount = amount,
            creator = participantsDomain.firstOrNull { it.paidShare > 0 }?.userId ?: "",
            shares = participantsDomain,
            splitMethod = state.splitMethod,
        )
      }
      onFinished()
    } catch (e: ApiCallError) {
      _uiState.update { it.copy(fieldErrors = e.fieldErrors) }
    } finally {
      _uiState.update { it.copy(isLoading = false) }
    }
  }

  override fun onBackClicked() {
    onFinished()
  }
}

class DefaultAddExpenseComponentFactory(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val profileRepository: ProfileRepository,
) : AddExpenseComponentFactory {
  override fun create(
      context: CContext,
      groupId: String,
      expenseId: String?,
      onNavigateToPayerFlow: () -> Unit,
      onNavigateToSplitFlow: () -> Unit,
      onFinished: () -> Unit,
  ): AddExpenseComponent =
      DefaultAddExpenseComponent(
          context = context,
          groupId = groupId,
          expenseId = expenseId,
          expenseRepository = expenseRepository,
          groupRepository = groupRepository,
          profileRepository = profileRepository,
          onNavigateToPayerFlow = onNavigateToPayerFlow,
          onNavigateToSplitFlow = onNavigateToSplitFlow,
          onFinished = onFinished,
      )
}

class FakeAddExpenseComponent(
    uiState: AddExpenseUiState =
        AddExpenseUiState(
            allParticipants = listOf("user1"),
            payAmounts = PayAmountsUiState.OnePerson(userId = "user1", amount = ""),
        ),
) : AddExpenseComponent {
  override val uiState: Value<AddExpenseUiState> = MutableValue(uiState)

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

  override fun navigateToPayerSelection() {}

  override fun navigateToQuickSplit() {}
}
