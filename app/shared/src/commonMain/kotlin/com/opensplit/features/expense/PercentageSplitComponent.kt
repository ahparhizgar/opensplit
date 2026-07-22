package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext

data class PercentageSplitUiState(
    val percentages: Map<String, String> = emptyMap(),
    val totalPercentage: Double = 0.0,
)

interface PercentageSplitComponent {
  val uiState: Value<PercentageSplitUiState>
  val initialParticipants: List<String>

  fun onParticipantPercentageChanged(userId: String, percentage: String)
}

class DefaultPercentageSplitComponent(
    context: CContext,
    override val initialParticipants: List<String>,
) : PercentageSplitComponent, CContext by context {
  private val _uiState = MutableValue(PercentageSplitUiState())
  override val uiState: Value<PercentageSplitUiState> = _uiState

  init {
    updateTotal()
  }

  override fun onParticipantPercentageChanged(userId: String, percentage: String) {
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

class FakePercentageSplitComponent(
    override val initialParticipants: List<String> = emptyList(),
    uiState: PercentageSplitUiState = PercentageSplitUiState(),
) : PercentageSplitComponent {
  override val uiState: Value<PercentageSplitUiState> = MutableValue(uiState)

  override fun onParticipantPercentageChanged(userId: String, percentage: String) {}
}
