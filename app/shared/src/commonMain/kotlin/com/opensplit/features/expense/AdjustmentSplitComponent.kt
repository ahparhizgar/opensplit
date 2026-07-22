package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext

data class AdjustmentSplitUiState(
    val adjustments: Map<String, String> = emptyMap(),
    val allParticipants: List<String> = emptyList(),
    val totalAdjustment: Double = 0.0,
)

interface AdjustmentSplitComponent {
  val uiState: Value<AdjustmentSplitUiState>
  val initialParticipants: List<String>

  fun onParticipantAdjustmentChanged(userId: String, adjustment: String)
}

class DefaultAdjustmentSplitComponent(
    context: CContext,
    override val initialParticipants: List<String>,
) : AdjustmentSplitComponent, CContext by context {
  private val _uiState = MutableValue(AdjustmentSplitUiState(allParticipants = initialParticipants))
  override val uiState: Value<AdjustmentSplitUiState> = _uiState

  override fun onParticipantAdjustmentChanged(userId: String, adjustment: String) {
    _uiState.update { state ->
      state.copy(adjustments = state.adjustments + Pair(userId, adjustment))
    }
  }
}

class FakeAdjustmentSplitComponent(
    override val initialParticipants: List<String> = emptyList(),
    uiState: AdjustmentSplitUiState = AdjustmentSplitUiState(allParticipants = initialParticipants),
) : AdjustmentSplitComponent {
  override val uiState: Value<AdjustmentSplitUiState> = MutableValue(uiState)

  override fun onParticipantAdjustmentChanged(userId: String, adjustment: String) {}
}
