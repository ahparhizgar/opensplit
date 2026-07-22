package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.dto.expense.SplitMethod

data class EquallySplitUiState(val userIds: Set<String> = emptySet()) {
  fun toMode(): SplitMethod.Equally {
    return SplitMethod.Equally(userIds.toList())
  }
}

interface EquallySplitComponent {
  val uiState: Value<EquallySplitUiState>
  val initialParticipants: List<String>

  fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean)
}

class DefaultEquallySplitComponent(
    context: CContext,
    override val initialParticipants: List<String>,
) : EquallySplitComponent, CContext by context {
  private val _uiState = MutableValue(EquallySplitUiState(userIds = initialParticipants.toSet()))
  override val uiState: Value<EquallySplitUiState> = _uiState

  override fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean) {
    _uiState.update { state ->
      state.copy(userIds = state.userIds.let { if (isIncluded) it + userId else it - userId })
    }
  }
}

class FakeEquallySplitComponent(
    override val initialParticipants: List<String> = emptyList(),
    uiState: EquallySplitUiState = EquallySplitUiState(userIds = initialParticipants.toSet()),
) : EquallySplitComponent {
  override val uiState: Value<EquallySplitUiState> = MutableValue(uiState)

  override fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean) {}
}
