package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext

data class UnequallySplitUiState(
    val amounts: Map<String, String> = emptyMap(),
    val remainingAmount: Double,
)

interface UnequallySplitComponent {
  val uiState: Value<UnequallySplitUiState>
  val initialParticipants: List<String>
  val totalAmount: Double

  fun onParticipantAmountChanged(userId: String, amount: String)
}

class DefaultUnequallySplitComponent(
    context: CContext,
    override val initialParticipants: List<String>,
    override val totalAmount: Double,
) : UnequallySplitComponent, CContext by context {
  private val _uiState = MutableValue(UnequallySplitUiState(remainingAmount = totalAmount))
  override val uiState: Value<UnequallySplitUiState> = _uiState

  init {
    updateRemaining()
  }

  override fun onParticipantAmountChanged(userId: String, amount: String) {
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

class FakeUnequallySplitComponent(
    override val initialParticipants: List<String> = emptyList(),
    override val totalAmount: Double = 0.0,
    uiState: UnequallySplitUiState = UnequallySplitUiState(remainingAmount = totalAmount),
) : UnequallySplitComponent {
  override val uiState: Value<UnequallySplitUiState> = MutableValue(uiState)

  override fun onParticipantAmountChanged(userId: String, amount: String) {}
}
