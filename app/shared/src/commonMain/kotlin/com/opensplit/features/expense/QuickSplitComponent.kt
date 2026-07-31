package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.household.FakeHouseholdMemberDtoFactory
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.repository.HouseholdRepository
import kotlinx.coroutines.launch

interface QuickSplitComponent {
  val uiState: Value<QuickSplitUiState>

  fun onOptionSelected(option: QuickSplitOption)

  fun onAdjustSplitClicked()

  interface Factory {
    fun create(
        context: CContext,
        allParticipants: List<String>,
        amountSum: Double,
        householdId: String,
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
}

data class QuickSplitUiState(
    val amountSum: Double,
    val you: HouseholdMemberDto? = null,
    val other: HouseholdMemberDto? = null,
)

class DefaultQuickSplitComponent(
    context: CContext,
    private val allParticipants: List<String>,
    amountSum: Double,
    householdId: String,
    private val onOptionSelected: (PayAmountsUiState, SplitMethod) -> Unit,
    private val onAdjustSplitClicked: () -> Unit,
    private val repository: HouseholdRepository,
) : QuickSplitComponent, CContext by context {

  private val _uiState = MutableValue(QuickSplitUiState(amountSum))
  override val uiState: Value<QuickSplitUiState> = _uiState
  private val scope = componentScope()

  init {
    scope.launch {
      val members = repository.getHousehold(householdId)!!.members
      val firstMember = members.find { it.userId == allParticipants[0] }
      val secondMember = members.find { it.userId == allParticipants[1] }
      _uiState.update {
        // Todo find out which one is the current user and set it as "you"
        it.copy(you = firstMember, other = secondMember)
      }
    }
  }

  override fun onOptionSelected(option: QuickSplitComponent.QuickSplitOption) {
    val uiStateValue = uiState.value
    when (option) {
      QuickSplitComponent.QuickSplitOption.YOU_PAID_SPLIT_EQUALLY -> {
        onOptionSelected(
            PayAmountsUiState.OnePerson(
                uiStateValue.you!!.userId,
                uiStateValue.amountSum.toString(),
            ),
            SplitMethod.Equally(allParticipants),
        )
      }
      QuickSplitComponent.QuickSplitOption.YOU_ARE_OWED_FULL_AMOUNT -> {
        onOptionSelected(
            PayAmountsUiState.OnePerson(
                uiStateValue.you!!.userId,
                uiStateValue.amountSum.toString(),
            ),
            SplitMethod.Unequally(mapOf(uiStateValue.other!!.userId to uiStateValue.amountSum)),
        )
      }
      QuickSplitComponent.QuickSplitOption.OTHER_PAID_SPLIT_EQUALLY -> {
        onOptionSelected(
            PayAmountsUiState.OnePerson(
                uiStateValue.other!!.userId,
                uiStateValue.amountSum.toString(),
            ),
            SplitMethod.Equally(allParticipants),
        )
      }
      QuickSplitComponent.QuickSplitOption.OTHER_IS_OWED_FULL_AMOUNT -> {
        onOptionSelected(
            PayAmountsUiState.OnePerson(
                uiStateValue.other!!.userId,
                uiStateValue.amountSum.toString(),
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
  ) : QuickSplitComponent.Factory {
    override fun create(
        context: CContext,
        allParticipants: List<String>,
        amountSum: Double,
        householdId: String,
        onOptionSelected: (PayAmountsUiState, SplitMethod) -> Unit,
        onAdjustSplitClicked: () -> Unit,
    ): QuickSplitComponent {
      return DefaultQuickSplitComponent(
          context = context,
          allParticipants = allParticipants,
          amountSum = amountSum,
          repository = repository,
          onOptionSelected = onOptionSelected,
          onAdjustSplitClicked = onAdjustSplitClicked,
          householdId = householdId,
      )
    }
  }
}

class FakeQuickSplitComponent(
    uiState: QuickSplitUiState =
        QuickSplitUiState(
            amountSum = 100.0,
            you = FakeHouseholdMemberDtoFactory.create1(),
            other = FakeHouseholdMemberDtoFactory.create2(),
        )
) : QuickSplitComponent {
  override val uiState: Value<QuickSplitUiState> = MutableValue(uiState)

  override fun onOptionSelected(option: QuickSplitComponent.QuickSplitOption) {}

  override fun onAdjustSplitClicked() {}
}
