package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SplitType

interface AdjustSplitComponent {
  val equallyComponent: EquallySplitComponent
  val exactComponent: ExactSplitComponent
  val percentageComponent: PercentageSplitComponent
  val sharesComponent: SharesSplitComponent
  val adjustmentComponent: AdjustmentSplitComponent

  fun onTabChanged(splitType: SplitType)

  fun onDoneClicked()

  interface Factory {
    fun create(
        context: CContext,
        initialParticipants: List<ParticipantState>,
        totalAmount: Double,
        onDone: (SplitMethod) -> Unit,
    ): AdjustSplitComponent
  }
}

class DefaultAdjustSplitComponent(
    context: CContext,
    private val initialParticipants: List<ParticipantState>,
    totalAmount: Double,
    private val onDone: (SplitMethod) -> Unit,
) : AdjustSplitComponent, CContext by context {

  private var currentSplitType = SplitType.EQUALLY

  override val equallyComponent = EquallySplitComponent(initialParticipants)
  override val exactComponent = ExactSplitComponent(initialParticipants, totalAmount)
  override val percentageComponent = PercentageSplitComponent(initialParticipants)
  override val sharesComponent = SharesSplitComponent(initialParticipants)
  override val adjustmentComponent = AdjustmentSplitComponent(initialParticipants)

  override fun onTabChanged(splitType: SplitType) {
    currentSplitType = splitType
  }

  override fun onDoneClicked() {
    val method =
        when (currentSplitType) {
          SplitType.EQUALLY ->
              SplitMethod.Equally(
                  equallyComponent.participants.filter { it.isIncluded }.map { it.userId }
              )
          SplitType.EXACT ->
              SplitMethod.Unequally(
                  exactComponent.participants.associate {
                    it.userId to (it.owedAmount.toDoubleOrNull() ?: 0.0)
                  }
              )
          SplitType.PERCENTAGE ->
              SplitMethod.Percentage(
                  percentageComponent.participants.associate {
                    it.userId to (it.percentage.toDoubleOrNull() ?: 0.0)
                  }
              )
          SplitType.SHARES ->
              SplitMethod.Shares(
                  sharesComponent.participants.associate {
                    it.userId to (it.shares.toDoubleOrNull() ?: 0.0)
                  }
              )
          SplitType.ADJUSTMENT ->
              SplitMethod.Adjustment(
                  adjustments =
                      adjustmentComponent.participants.associate {
                        it.userId to (it.adjustment.toDoubleOrNull() ?: 0.0)
                      },
                  equallyUserIds =
                      adjustmentComponent.participants.filter { it.isIncluded }.map { it.userId },
              )
        }
    onDone(method)
  }

  class Factory : AdjustSplitComponent.Factory {
    override fun create(
        context: CContext,
        initialParticipants: List<ParticipantState>,
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
data class EquallySplitUiState(val participants: List<ParticipantState> = emptyList())

class EquallySplitComponent(initialParticipants: List<ParticipantState>) {
  private val _uiState = MutableValue(EquallySplitUiState(initialParticipants))
  val uiState: Value<EquallySplitUiState> = _uiState

  val participants: List<ParticipantState>
    get() = uiState.value.participants

  fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean) {
    _uiState.update { state ->
      state.copy(
          participants =
              state.participants.map {
                if (it.userId == userId) it.copy(isIncluded = isIncluded) else it
              }
      )
    }
  }
}

// Exact Split
data class ExactSplitUiState(
    val participants: List<ParticipantState> = emptyList(),
    val totalAmount: Double,
    val remainingAmount: Double = 0.0,
)

class ExactSplitComponent(initialParticipants: List<ParticipantState>, val totalAmount: Double) {
  private val _uiState = MutableValue(ExactSplitUiState(initialParticipants, totalAmount))
  val uiState: Value<ExactSplitUiState> = _uiState

  init {
    updateRemaining()
  }

  val participants: List<ParticipantState>
    get() = uiState.value.participants

  fun onParticipantAmountChanged(userId: String, amount: String) {
    _uiState.update { state ->
      state.copy(
          participants =
              state.participants.map {
                if (it.userId == userId) it.copy(owedAmount = amount) else it
              }
      )
    }
    updateRemaining()
  }

  private fun updateRemaining() {
    _uiState.update { state ->
      val totalOwed = state.participants.sumOf { it.owedAmount.toDoubleOrNull() ?: 0.0 }
      state.copy(remainingAmount = totalAmount - totalOwed)
    }
  }
}

// Percentage Split
data class PercentageSplitUiState(
    val participants: List<ParticipantState> = emptyList(),
    val totalPercentage: Double = 0.0,
)

class PercentageSplitComponent(initialParticipants: List<ParticipantState>) {
  private val _uiState = MutableValue(PercentageSplitUiState(initialParticipants))
  val uiState: Value<PercentageSplitUiState> = _uiState

  init {
    updateTotal()
  }

  val participants: List<ParticipantState>
    get() = uiState.value.participants

  fun onParticipantPercentageChanged(userId: String, percentage: String) {
    _uiState.update { state ->
      state.copy(
          participants =
              state.participants.map {
                if (it.userId == userId) it.copy(percentage = percentage) else it
              }
      )
    }
    updateTotal()
  }

  private fun updateTotal() {
    _uiState.update { state ->
      val total = state.participants.sumOf { it.percentage.toDoubleOrNull() ?: 0.0 }
      state.copy(totalPercentage = total)
    }
  }
}

// Shares Split
data class SharesSplitUiState(
    val participants: List<ParticipantState> = emptyList(),
    val totalShares: Double = 0.0,
)

class SharesSplitComponent(initialParticipants: List<ParticipantState>) {
  private val _uiState = MutableValue(SharesSplitUiState(initialParticipants))
  val uiState: Value<SharesSplitUiState> = _uiState

  init {
    updateTotal()
  }

  val participants: List<ParticipantState>
    get() = uiState.value.participants

  fun onParticipantSharesChanged(userId: String, shares: String) {
    _uiState.update { state ->
      state.copy(
          participants =
              state.participants.map { if (it.userId == userId) it.copy(shares = shares) else it }
      )
    }
    updateTotal()
  }

  private fun updateTotal() {
    _uiState.update { state ->
      val total = state.participants.sumOf { it.shares.toDoubleOrNull() ?: 0.0 }
      state.copy(totalShares = total)
    }
  }
}

// Adjustment Split
data class AdjustmentSplitUiState(
    val participants: List<ParticipantState> = emptyList(),
    val totalAdjustment: Double = 0.0,
)

class AdjustmentSplitComponent(initialParticipants: List<ParticipantState>) {
  private val _uiState = MutableValue(AdjustmentSplitUiState(initialParticipants))
  val uiState: Value<AdjustmentSplitUiState> = _uiState

  init {
    updateTotal()
  }

  val participants: List<ParticipantState>
    get() = uiState.value.participants

  fun onParticipantAdjustmentChanged(userId: String, adjustment: String) {
    _uiState.update { state ->
      state.copy(
          participants =
              state.participants.map {
                if (it.userId == userId) it.copy(adjustment = adjustment) else it
              }
      )
    }
    updateTotal()
  }

  private fun updateTotal() {
    _uiState.update { state ->
      val total = state.participants.sumOf { it.adjustment.toDoubleOrNull() ?: 0.0 }
      state.copy(totalAdjustment = total)
    }
  }
}

class FakeAdjustSplitComponent(
    override val equallyComponent: EquallySplitComponent = EquallySplitComponent(emptyList()),
    override val exactComponent: ExactSplitComponent = ExactSplitComponent(emptyList(), 0.0),
    override val percentageComponent: PercentageSplitComponent =
        PercentageSplitComponent(emptyList()),
    override val sharesComponent: SharesSplitComponent = SharesSplitComponent(emptyList()),
    override val adjustmentComponent: AdjustmentSplitComponent =
        AdjustmentSplitComponent(emptyList()),
) : AdjustSplitComponent {
  override fun onTabChanged(splitType: SplitType) {}

  override fun onDoneClicked() {}
}
