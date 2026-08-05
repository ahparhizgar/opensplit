package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.domain.Member

data class AdjustmentSplitUiState(
    val adjustments: Map<String, String> = emptyMap(),
    val participants: List<Member> = emptyList(),
    val totalAdjustment: Double = 0.0,
)

interface AdjustmentSplitComponent {
  val uiState: Value<AdjustmentSplitUiState>
  val participants: List<Member>

  fun onParticipantAdjustmentChanged(userId: String, adjustment: String)
}

class DefaultAdjustmentSplitComponent(
    context: CContext,
    override val participants: List<Member>,
) : AdjustmentSplitComponent, CContext by context {
  private val _uiState = MutableValue(AdjustmentSplitUiState(participants = participants))
  override val uiState: Value<AdjustmentSplitUiState> = _uiState

  override fun onParticipantAdjustmentChanged(userId: String, adjustment: String) {
    _uiState.update { state ->
      state.copy(adjustments = state.adjustments + Pair(userId, adjustment))
    }
  }
}

class FakeAdjustmentSplitComponent(
    override val participants: List<Member> = emptyList(),
    uiState: AdjustmentSplitUiState = AdjustmentSplitUiState(participants = participants),
) : AdjustmentSplitComponent {
  override val uiState: Value<AdjustmentSplitUiState> = MutableValue(uiState)

  override fun onParticipantAdjustmentChanged(userId: String, adjustment: String) {}
}
