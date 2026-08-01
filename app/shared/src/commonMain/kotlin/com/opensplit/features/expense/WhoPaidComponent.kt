package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.domain.Member

interface WhoPaidComponent {
  val uiState: Value<WhoPaidUiState>

  fun onParticipantSelected(userId: String)

  fun onMultiplePeopleClicked()

  interface Factory {
    fun create(
        context: CContext,
        participants: List<Member>,
        selectedUserId: String?,
        onParticipantSelected: (String) -> Unit,
        onMultiplePeopleClicked: () -> Unit,
    ): WhoPaidComponent
  }
}

data class WhoPaidUiState(
    val participants: List<Member>,
    val selectedUserId: String?,
)

class DefaultWhoPaidComponent(
    context: CContext,
    participants: List<Member>,
    selectedUserId: String?,
    private val onParticipantSelected: (String) -> Unit,
    private val onMultiplePeopleClicked: () -> Unit,
) : WhoPaidComponent, CContext by context {

  private val _uiState =
      MutableValue(
          WhoPaidUiState(
              participants = participants,
              selectedUserId = selectedUserId,
          )
      )

  override val uiState: Value<WhoPaidUiState> = _uiState

  override fun onParticipantSelected(userId: String) {
    onParticipantSelected.invoke(userId)
  }

  override fun onMultiplePeopleClicked() {
    onMultiplePeopleClicked.invoke()
  }

  class Factory : WhoPaidComponent.Factory {
    override fun create(
        context: CContext,
        participants: List<Member>,
        selectedUserId: String?,
        onParticipantSelected: (String) -> Unit,
        onMultiplePeopleClicked: () -> Unit,
    ): WhoPaidComponent {
      return DefaultWhoPaidComponent(
          context = context,
          participants = participants,
          selectedUserId = selectedUserId,
          onParticipantSelected = onParticipantSelected,
          onMultiplePeopleClicked = onMultiplePeopleClicked,
      )
    }
  }
}

class FakeWhoPaidComponent(uiState: WhoPaidUiState = WhoPaidUiState(emptyList(), "user1")) :
    WhoPaidComponent {
  override val uiState: Value<WhoPaidUiState> = MutableValue(uiState)

  override fun onParticipantSelected(userId: String) {}

  override fun onMultiplePeopleClicked() {}
}
