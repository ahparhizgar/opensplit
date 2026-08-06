package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.domain.Member
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.repository.HouseholdRepository
import com.opensplit.repository.ProfileRepository
import kotlinx.coroutines.launch

interface QuickSplitComponent {
  val uiState: Value<QuickSplitUiState>

  fun onOptionSelected(option: QuickSplitOption)

  fun onAdjustSplitClicked()

  interface Factory {
    fun create(
        context: CContext,
        allParticipants: List<String>,
        amountText: String,
        amountSum: Double,
        householdId: String,
        initialOption: QuickSplitOption? = null,
        onOptionSelected: (PayAmountsUiState, SplitMethod) -> Unit,
        onAdjustSplitClicked: () -> Unit,
    ): QuickSplitComponent
  }

  enum class QuickSplitOption {
    YOU_PAID_SPLIT_EQUALLY,
    YOU_ARE_OWED_FULL_AMOUNT,
    OTHER_PAID_SPLIT_EQUALLY,
    OTHER_IS_OWED_FULL_AMOUNT,
  }

  companion object {
    fun getOption(
        payAmounts: PayAmountsUiState,
        splitMethod: SplitMethod,
        youId: String,
        otherId: String,
        amountSum: Double,
        allParticipants: List<String>,
    ): QuickSplitOption? {
      if (payAmounts !is PayAmountsUiState.OnePerson) return null
      val amount = payAmounts.amount.toDoubleOrNull() ?: 0.0
      if (amount != amountSum) return null

      return when {
        payAmounts.userId == youId &&
            splitMethod is SplitMethod.Equally &&
            splitMethod.userIds.toSet() == allParticipants.toSet() ->
            QuickSplitOption.YOU_PAID_SPLIT_EQUALLY
        payAmounts.userId == youId &&
            splitMethod is SplitMethod.Unequally &&
            splitMethod.amounts.keys == setOf(otherId) -> QuickSplitOption.YOU_ARE_OWED_FULL_AMOUNT
        payAmounts.userId == otherId &&
            splitMethod is SplitMethod.Equally &&
            splitMethod.userIds.toSet() == allParticipants.toSet() ->
            QuickSplitOption.OTHER_PAID_SPLIT_EQUALLY
        payAmounts.userId == otherId &&
            splitMethod is SplitMethod.Unequally &&
            splitMethod.amounts.keys == setOf(youId) -> QuickSplitOption.OTHER_IS_OWED_FULL_AMOUNT
        else -> null
      }
    }
  }
}

data class QuickSplitUiState(
    val amountSum: Double,
    val you: Member? = null,
    val other: Member? = null,
    val selectedOption: QuickSplitComponent.QuickSplitOption? = null,
)

class DefaultQuickSplitComponent(
    context: CContext,
    private val allParticipants: List<String>,
    private val amountText: String,
    amountSum: Double,
    householdId: String,
    initialOption: QuickSplitComponent.QuickSplitOption?,
    private val onOptionSelected: (PayAmountsUiState, SplitMethod) -> Unit,
    private val onAdjustSplitClicked: () -> Unit,
    private val repository: HouseholdRepository,
    private val profileRepository: ProfileRepository,
) : QuickSplitComponent, CContext by context {

  private val _uiState = MutableValue(QuickSplitUiState(amountSum, selectedOption = initialOption))
  override val uiState: Value<QuickSplitUiState> = _uiState
  private val scope = componentScope()

  init {
    scope.launch {
      val household = repository.getHousehold(householdId)
      val members =
          if (household == null || household.members.isEmpty()) {
            repository.refreshHousehold(householdId)?.members ?: emptyList()
          } else {
            household.members
          }

      val currentUserId = profileRepository.profile.value?.id
      val otherMember = members.firstOrNull { it.userId != currentUserId }
      val firstMember = members.firstOrNull { it.userId == currentUserId }

      _uiState.update { it.copy(you = firstMember, other = otherMember) }
    }
  }

  override fun onOptionSelected(option: QuickSplitComponent.QuickSplitOption) {
    _uiState.update { it.copy(selectedOption = option) }
    val uiStateValue = uiState.value
    when (option) {
      QuickSplitComponent.QuickSplitOption.YOU_PAID_SPLIT_EQUALLY -> {
        onOptionSelected(
            PayAmountsUiState.OnePerson(
                uiStateValue.you!!.userId,
                amountText,
            ),
            SplitMethod.Equally(allParticipants),
        )
      }
      QuickSplitComponent.QuickSplitOption.YOU_ARE_OWED_FULL_AMOUNT -> {
        onOptionSelected(
            PayAmountsUiState.OnePerson(
                uiStateValue.you!!.userId,
                amountText,
            ),
            SplitMethod.Unequally(mapOf(uiStateValue.other!!.userId to uiStateValue.amountSum)),
        )
      }
      QuickSplitComponent.QuickSplitOption.OTHER_PAID_SPLIT_EQUALLY -> {
        onOptionSelected(
            PayAmountsUiState.OnePerson(
                uiStateValue.other!!.userId,
                amountText,
            ),
            SplitMethod.Equally(allParticipants),
        )
      }
      QuickSplitComponent.QuickSplitOption.OTHER_IS_OWED_FULL_AMOUNT -> {
        onOptionSelected(
            PayAmountsUiState.OnePerson(
                uiStateValue.other!!.userId,
                amountText,
            ),
            SplitMethod.Unequally(mapOf(uiStateValue.you!!.userId to uiStateValue.amountSum)),
        )
      }
    }
  }

  override fun onAdjustSplitClicked() {
    onAdjustSplitClicked.invoke()
  }

  class Factory(
      private val repository: HouseholdRepository,
      private val profileRepository: ProfileRepository,
  ) : QuickSplitComponent.Factory {
    override fun create(
        context: CContext,
        allParticipants: List<String>,
        amountText: String,
        amountSum: Double,
        householdId: String,
        initialOption: QuickSplitComponent.QuickSplitOption?,
        onOptionSelected: (PayAmountsUiState, SplitMethod) -> Unit,
        onAdjustSplitClicked: () -> Unit,
    ): QuickSplitComponent {
      return DefaultQuickSplitComponent(
          context = context,
          allParticipants = allParticipants,
          amountText = amountText,
          amountSum = amountSum,
          repository = repository,
          profileRepository = profileRepository,
          onOptionSelected = onOptionSelected,
          onAdjustSplitClicked = onAdjustSplitClicked,
          householdId = householdId,
          initialOption = initialOption,
      )
    }
  }
}

class FakeQuickSplitComponent(
    uiState: QuickSplitUiState =
        QuickSplitUiState(
            amountSum = 100.0,
            you = FakeMemberFactory.create1(),
            other = FakeMemberFactory.create2(),
        )
) : QuickSplitComponent {
  override val uiState: Value<QuickSplitUiState> = MutableValue(uiState)

  override fun onOptionSelected(option: QuickSplitComponent.QuickSplitOption) {}

  override fun onAdjustSplitClicked() {}
}
