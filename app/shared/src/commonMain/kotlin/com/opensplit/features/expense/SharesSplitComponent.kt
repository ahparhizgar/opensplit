package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.domain.Member

data class SharesSplitUiState(
    val shares: Map<String, String> = emptyMap(),
) {
  val totalShares: Int
    get() = shares.values.sumOf { it.toIntOrNull() ?: 0 }
}

interface SharesSplitComponent {
  val uiState: Value<SharesSplitUiState>
  val participants: List<Member>

  fun onParticipantSharesChanged(userId: String, shares: String)
}

class DefaultSharesSplitComponent(
    context: CContext,
    override val participants: List<Member>,
    initialShares: Map<String, String> = emptyMap(),
) : SharesSplitComponent, CContext by context {
  private val _uiState = MutableValue(SharesSplitUiState(shares = initialShares))
  override val uiState: Value<SharesSplitUiState> = _uiState

  override fun onParticipantSharesChanged(userId: String, shares: String) {
    _uiState.update { state -> state.copy(shares = state.shares + Pair(userId, shares)) }
  }
}

class FakeSharesSplitComponent(
    override val participants: List<Member> = emptyList(),
    uiState: SharesSplitUiState = SharesSplitUiState(),
) : SharesSplitComponent {
  override val uiState: Value<SharesSplitUiState> = MutableValue(uiState)

  override fun onParticipantSharesChanged(userId: String, shares: String) {}
}
