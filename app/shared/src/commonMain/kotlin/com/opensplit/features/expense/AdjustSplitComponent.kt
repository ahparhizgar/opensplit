package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SplitType

interface AdjustSplitComponent {
  val equallyComponent: EquallySplitComponent
  val unequallyComponent: UnequallySplitComponent
  val percentageComponent: PercentageSplitComponent
  val sharesComponent: SharesSplitComponent
  val adjustmentComponent: AdjustmentSplitComponent

  fun onTabChanged(splitType: SplitType)

  fun onDoneClicked()

  interface Factory {
    fun create(
        context: CContext,
        initialParticipants: List<String>,
        totalAmount: Double,
        onDone: (SplitMethod) -> Unit,
    ): AdjustSplitComponent
  }
}

class DefaultAdjustSplitComponent(
    context: CContext,
    initialParticipants: List<String>,
    totalAmount: Double,
    private val onDone: (SplitMethod) -> Unit,
) : AdjustSplitComponent, CContext by context {

  private var currentSplitType = SplitType.EQUALLY

  override val equallyComponent = EquallySplitComponent(initialParticipants)
  override val unequallyComponent = UnequallySplitComponent(initialParticipants, totalAmount)
  override val percentageComponent = PercentageSplitComponent(initialParticipants)
  override val sharesComponent = SharesSplitComponent(initialParticipants)
  override val adjustmentComponent = AdjustmentSplitComponent(initialParticipants)

  override fun onTabChanged(splitType: SplitType) {
    currentSplitType = splitType
  }

  override fun onDoneClicked() {
    val method =
        when (currentSplitType) {
          SplitType.EQUALLY -> equallyComponent.uiState.value.toMode()
          SplitType.Unequally ->
              SplitMethod.Unequally(
                  unequallyComponent.uiState.value.amounts.mapValues {
                    it.value.toDoubleOrNull() ?: 0.0
                  }
              )
          SplitType.PERCENTAGE ->
              SplitMethod.Percentage(
                  percentageComponent.uiState.value.percentages.mapValues {
                    it.value.toDoubleOrNull() ?: 0.0
                  }
              )
          SplitType.SHARES ->
              SplitMethod.Shares(
                  sharesComponent.uiState.value.shares.mapValues { it.value.toIntOrNull() ?: 0 }
              )
          SplitType.ADJUSTMENT ->
              SplitMethod.Adjustment(
                  adjustments =
                      adjustmentComponent.uiState.value.adjustments.mapValues {
                        it.value.toDoubleOrNull() ?: 0.0
                      },
              )
        }
    onDone(method)
  }

  class Factory : AdjustSplitComponent.Factory {
    override fun create(
        context: CContext,
        initialParticipants: List<String>,
        totalAmount: Double,
        onDone: (SplitMethod) -> Unit,
    ): AdjustSplitComponent =
        DefaultAdjustSplitComponent(context, initialParticipants, totalAmount, onDone)
  }
}

data class SplitParticipantState(
    val userId: String,
    val name: String,
    val value: String = "",
    val isIncluded: Boolean = true,
    val owedAmount: Double = 0.0,
)

// Equally Split
data class EquallySplitUiState(val userIds: Set<String> = emptySet()) {
  fun toMode(): SplitMethod.Equally {
    return SplitMethod.Equally(userIds.toList())
  }
}

class EquallySplitComponent(val initialParticipants: List<String>) {
  private val _uiState = MutableValue(EquallySplitUiState(userIds = initialParticipants.toSet()))
  val uiState: Value<EquallySplitUiState> = _uiState

  fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean) {
    _uiState.update { state ->
      state.copy(userIds = state.userIds.let { if (isIncluded) it + userId else it - userId })
    }
  }
}

// Exact Split
data class ExactSplitUiState(
    val amounts: Map<String, String> = emptyMap(),
    val remainingAmount: Double,
)

class UnequallySplitComponent(
    val initialParticipants: List<String>,
    val totalAmount: Double,
) {
  private val _uiState = MutableValue(ExactSplitUiState(remainingAmount = totalAmount))
  val uiState: Value<ExactSplitUiState> = _uiState

  init {
    updateRemaining()
  }

  fun onParticipantAmountChanged(userId: String, amount: String) {
    _uiState.update { state -> state.copy(amounts = state.amounts + Pair(userId, amount)) }
    updateRemaining()
  }

  private fun updateRemaining() {
    _uiState.update { state ->
      val totalOwed = state.amounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
      state.copy(remainingAmount = totalAmount - totalOwed)
    }
  }
}

// Percentage Split
data class PercentageSplitUiState(
    val percentages: Map<String, String> = emptyMap(),
    val totalPercentage: Double = 0.0,
)

class PercentageSplitComponent(val initialParticipants: List<String>) {
  private val _uiState = MutableValue(PercentageSplitUiState())
  val uiState: Value<PercentageSplitUiState> = _uiState

  init {
    updateTotal()
  }

  fun onParticipantPercentageChanged(userId: String, percentage: String) {
    _uiState.update { state ->
      state.copy(percentages = state.percentages + Pair(userId, percentage))
    }
    updateTotal()
  }

  private fun updateTotal() {
    _uiState.update { state ->
      val total = state.percentages.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
      state.copy(totalPercentage = total)
    }
  }
}

// Shares Split
data class SharesSplitUiState(
    val shares: Map<String, String> = emptyMap(),
    val totalShares: Int = 0,
)

class SharesSplitComponent(val initialParticipants: List<String>) {
  private val _uiState = MutableValue(SharesSplitUiState())
  val uiState: Value<SharesSplitUiState> = _uiState

  fun onParticipantSharesChanged(userId: String, shares: String) {
    _uiState.update { state -> state.copy(shares = state.shares + Pair(userId, shares)) }
    updateTotal()
  }

  private fun updateTotal() {
    _uiState.update { state ->
      val total = state.shares.values.sumOf { it.toIntOrNull() ?: 0 }
      state.copy(totalShares = total)
    }
  }
}

// Adjustment Split
data class AdjustmentSplitUiState(
    val adjustments: Map<String, String> = emptyMap(),
    val allParticipants: List<String> = emptyList(),
    val totalAdjustment: Double = 0.0,
)

class AdjustmentSplitComponent(val initialParticipants: List<String>) {
  private val _uiState = MutableValue(AdjustmentSplitUiState(allParticipants = initialParticipants))
  val uiState: Value<AdjustmentSplitUiState> = _uiState

  fun onParticipantAdjustmentChanged(userId: String, adjustment: String) {
    _uiState.update { state ->
      state.copy(adjustments = state.adjustments + Pair(userId, adjustment))
    }
  }
}

class FakeAdjustSplitComponent(
    override val equallyComponent: EquallySplitComponent = EquallySplitComponent(emptyList()),
    override val unequallyComponent: UnequallySplitComponent =
        UnequallySplitComponent(emptyList(), 0.0),
    override val percentageComponent: PercentageSplitComponent =
        PercentageSplitComponent(emptyList()),
    override val sharesComponent: SharesSplitComponent = SharesSplitComponent(emptyList()),
    override val adjustmentComponent: AdjustmentSplitComponent =
        AdjustmentSplitComponent(emptyList()),
) : AdjustSplitComponent {
  override fun onTabChanged(splitType: SplitType) {}

  override fun onDoneClicked() {}
}
