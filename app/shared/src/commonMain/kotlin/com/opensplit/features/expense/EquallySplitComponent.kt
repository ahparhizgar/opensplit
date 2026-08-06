package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.domain.Member
import com.opensplit.dto.expense.SplitMethod

data class EquallySplitUiState(val userIds: Set<String> = emptySet()) {
  fun toMode(): SplitMethod.Equally {
    return SplitMethod.Equally(userIds.toList())
  }
}

interface EquallySplitComponent {
  val uiState: Value<EquallySplitUiState>
  val participants: List<Member>

  fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean)
}

class DefaultEquallySplitComponent(
    context: CContext,
    override val participants: List<Member>,
    initialUserIds: Set<String> = participants.map { it.userId }.toSet(),
) : EquallySplitComponent, CContext by context {
  private val _uiState = MutableValue(EquallySplitUiState(userIds = initialUserIds))
  override val uiState: Value<EquallySplitUiState> = _uiState

  override fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean) {
    _uiState.update { state ->
      state.copy(userIds = state.userIds.let { if (isIncluded) it + userId else it - userId })
    }
  }
}

class FakeEquallySplitComponent(
    override val participants: List<Member> = emptyList(),
    uiState: EquallySplitUiState =
        EquallySplitUiState(userIds = participants.map { it.userId }.toSet()),
) : EquallySplitComponent {
  override val uiState: Value<EquallySplitUiState> = MutableValue(uiState)

  override fun onParticipantInclusionChanged(userId: String, isIncluded: Boolean) {}
}
