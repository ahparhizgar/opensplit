package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext

interface WhoPaidComponent {
  val uiState: Value<WhoPaidUiState>

  fun onParticipantSelected(userId: String)

  fun onMultiplePeopleClicked()

  interface Factory {
    fun create(
        context: CContext,
        allParticipants: List<String>,
        selectedUserId: String?,
        onParticipantSelected: (String) -> Unit,
        onMultiplePeopleClicked: () -> Unit,
    ): WhoPaidComponent
  }
}

data class WhoPaidUiState(
    val allParticipants: List<String>,
    val selectedUserId: String?,
)

class DefaultWhoPaidComponent(
    context: CContext,
    allParticipants: List<String>,
    selectedUserId: String?,
    private val onParticipantSelected: (String) -> Unit,
    private val onMultiplePeopleClicked: () -> Unit,
) : WhoPaidComponent, CContext by context {

  private val _uiState =
      MutableValue(
          WhoPaidUiState(
              allParticipants = allParticipants,
              selectedUserId = selectedUserId,
          )
      )

  override val uiState: Value<WhoPaidUiState> = _uiState

  override fun onParticipantSelected(userId: String) {
    onParticipantSelected(userId)
  }

  override fun onMultiplePeopleClicked() {
    onMultiplePeopleClicked()
  }

  class Factory : WhoPaidComponent.Factory {
    override fun create(
        context: CContext,
        allParticipants: List<String>,
        selectedUserId: String?,
        onParticipantSelected: (String) -> Unit,
        onMultiplePeopleClicked: () -> Unit,
    ): WhoPaidComponent {
      return DefaultWhoPaidComponent(
          context = context,
          allParticipants = allParticipants,
          selectedUserId = selectedUserId,
          onParticipantSelected = onParticipantSelected,
          onMultiplePeopleClicked = onMultiplePeopleClicked,
      )
    }
  }
}

class FakeWhoPaidComponent(
    uiState: WhoPaidUiState = WhoPaidUiState(listOf("user1", "user2"), "user1")
) : WhoPaidComponent {
  override val uiState: Value<WhoPaidUiState> = MutableValue(uiState)

  override fun onParticipantSelected(userId: String) {}

  override fun onMultiplePeopleClicked() {}
}
