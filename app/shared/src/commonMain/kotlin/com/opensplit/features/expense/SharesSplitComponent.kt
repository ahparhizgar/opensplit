package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext

data class SharesSplitUiState(
    val shares: Map<String, String> = emptyMap(),
    val totalShares: Int = 0,
)

interface SharesSplitComponent {
  val uiState: Value<SharesSplitUiState>
  val initialParticipants: List<String>

  fun onParticipantSharesChanged(userId: String, shares: String)
}

class DefaultSharesSplitComponent(
    context: CContext,
    override val initialParticipants: List<String>,
) : SharesSplitComponent, CContext by context {
  private val _uiState = MutableValue(SharesSplitUiState())
  override val uiState: Value<SharesSplitUiState> = _uiState

  override fun onParticipantSharesChanged(userId: String, shares: String) {
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

class FakeSharesSplitComponent(
    override val initialParticipants: List<String> = emptyList(),
    uiState: SharesSplitUiState = SharesSplitUiState(),
) : SharesSplitComponent {
  override val uiState: Value<SharesSplitUiState> = MutableValue(uiState)

  override fun onParticipantSharesChanged(userId: String, shares: String) {}
}
