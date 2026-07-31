package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.dto.expense.SplitMethod

interface QuickSplitComponent {
  val uiState: Value<QuickSplitUiState>

  fun onOptionSelected(payerId: String, splitMethod: SplitMethod)

  fun onAdjustSplitClicked()

  interface Factory {
    fun create(
        context: CContext,
        allParticipants: List<String>,
        amountSum: Double,
        onOptionSelected: (PayAmountsUiState, SplitMethod) -> Unit,
        onAdjustSplitClicked: () -> Unit,
    ): QuickSplitComponent
  }
}

data class QuickSplitUiState(
    val allParticipants: List<String>,
    val amountSum: Double,
)

class DefaultQuickSplitComponent(
    context: CContext,
    allParticipants: List<String>,
    amountSum: Double,
    private val onOptionSelected: (PayAmountsUiState, SplitMethod) -> Unit,
    private val onAdjustSplitClicked: () -> Unit,
) : QuickSplitComponent, CContext by context {

  private val _uiState = MutableValue(QuickSplitUiState(allParticipants, amountSum))
  override val uiState: Value<QuickSplitUiState> = _uiState

  override fun onOptionSelected(payerId: String, splitMethod: SplitMethod) {
    onOptionSelected(
        PayAmountsUiState.OnePerson(payerId, uiState.value.amountSum.toString()),
        splitMethod,
    )
  }

  override fun onAdjustSplitClicked() {
    onAdjustSplitClicked()
  }

  class Factory : QuickSplitComponent.Factory {
    override fun create(
        context: CContext,
        allParticipants: List<String>,
        amountSum: Double,
        onOptionSelected: (PayAmountsUiState, SplitMethod) -> Unit,
        onAdjustSplitClicked: () -> Unit,
    ): QuickSplitComponent {
      return DefaultQuickSplitComponent(
          context = context,
          allParticipants = allParticipants,
          amountSum = amountSum,
          onOptionSelected = onOptionSelected,
          onAdjustSplitClicked = onAdjustSplitClicked,
      )
    }
  }
}

class FakeQuickSplitComponent(
    uiState: QuickSplitUiState = QuickSplitUiState(listOf("user1", "user2"), 100.0)
) : QuickSplitComponent {
  override val uiState: Value<QuickSplitUiState> = MutableValue(uiState)

  override fun onOptionSelected(payerId: String, splitMethod: SplitMethod) {}

  override fun onAdjustSplitClicked() {}
}
