package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.domain.FakeHouseholdFactory
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.domain.Household
import com.opensplit.dto.expense.ParticipantAmount

interface PaidAmountsComponent {
  val uiState: Value<PaidAmountsUiState>

  fun onParticipantAmountChanged(userId: String, amount: String)

  fun onDone()

  interface Factory {
    fun create(
        initial: PayAmounts,
        household: Household,
        onDone: (PayAmountsUiState) -> Unit,
    ): PaidAmountsComponent
  }
}

data class ParticipantValue(val userId: String, val name: String, val value: String)

data class PaidAmountsUiState(
    val allParticipantAmounts: List<ParticipantValue>,
    val goalAmount: Double?,
)

class DefaultPaidAmountsComponent(
    initial: PayAmounts,
    household: Household,
    private val onDone: (PayAmountsUiState) -> Unit,
) : PaidAmountsComponent {

  private val _uiState =
      MutableValue(
          PaidAmountsUiState(
              goalAmount =
                  when (initial) {
                    is PayAmounts.OnePerson -> initial.amount?.takeIf { it > 0.0 }
                    is PayAmounts.MultiplePeople -> null
                  },
              allParticipantAmounts =
                  when (initial) {
                    is PayAmounts.OnePerson ->
                        household.members.map { member ->
                          if (member.userId == initial.userId)
                              ParticipantValue(
                                  initial.userId,
                                  member.name,
                                  initial.amount.toString(),
                              )
                          else ParticipantValue(member.userId, member.name, "")
                        }

                    is PayAmounts.MultiplePeople ->
                        household.members.map { member ->
                          initial.amounts
                              .find { it.userId == member.userId }
                              ?.let {
                                ParticipantValue(member.userId, member.name, it.amount.toString())
                              } ?: ParticipantValue(member.userId, member.name, "")
                        }
                  },
          )
      )

  override val uiState: Value<PaidAmountsUiState> = _uiState

  override fun onParticipantAmountChanged(userId: String, amount: String) {
    val updatedAmounts =
        uiState.value.allParticipantAmounts.map {
          if (it.userId == userId) it.copy(value = amount) else it
        }
    _uiState.update { it.copy(allParticipantAmounts = updatedAmounts) }
  }

  override fun onDone() {
    val nonZeroAmounts =
        uiState.value.allParticipantAmounts.filter {
          it.value.toDoubleOrNull()?.takeIf { it > 0.0 } != null
        }
    onDone(
        if (nonZeroAmounts.size == 1) {
          val single = nonZeroAmounts.first()
          PayAmountsUiState.OnePerson(single.userId, single.value)
        } else {
          PayAmountsUiState.MultiplePeople(uiState.value.allParticipantAmounts)
        }
    )
  }

  class Factory : PaidAmountsComponent.Factory {
    override fun create(
        initial: PayAmounts,
        household: Household,
        onDone: (PayAmountsUiState) -> Unit,
    ): PaidAmountsComponent {
      return DefaultPaidAmountsComponent(initial = initial, household = household, onDone = onDone)
    }
  }
}

class FakePaidAmountsComponent(
    initial: PayAmounts = PayAmounts.OnePerson("user-1", 100.0),
    household: Household =
        FakeHouseholdFactory.create(
            members = listOf(FakeMemberFactory.create(userId = "user-1", isCurrentUser = true))
        ),
    private val onDone: (PayAmounts) -> Unit = {},
) : PaidAmountsComponent {

  private val _uiState =
      MutableValue(
          PaidAmountsUiState(
              goalAmount =
                  when (initial) {
                    is PayAmounts.OnePerson -> initial.amount?.takeIf { it > 0.0 }
                    is PayAmounts.MultiplePeople -> null
                  },
              allParticipantAmounts =
                  when (initial) {
                    is PayAmounts.OnePerson ->
                        household.members.map { member ->
                          if (member.userId == initial.userId)
                              ParticipantValue(
                                  initial.userId,
                                  member.name,
                                  initial.amount.toString(),
                              )
                          else ParticipantValue(member.userId, member.name, "")
                        }

                    is PayAmounts.MultiplePeople ->
                        household.members.map { member ->
                          initial.amounts
                              .find { it.userId == member.userId }
                              ?.let {
                                ParticipantValue(member.userId, member.name, it.amount.toString())
                              } ?: ParticipantValue(member.userId, member.name, "")
                        }
                  },
          )
      )

  override val uiState: Value<PaidAmountsUiState> = _uiState

  override fun onParticipantAmountChanged(userId: String, amount: String) {
    val updatedAmounts =
        uiState.value.allParticipantAmounts.map {
          if (it.userId == userId) it.copy(value = amount) else it
        }
    _uiState.update { it.copy(allParticipantAmounts = updatedAmounts) }
  }

  override fun onDone() {
    val nonZeroAmounts =
        uiState.value.allParticipantAmounts.mapNotNull {
          it.value
              .toDoubleOrNull()
              ?.takeIf { it > 0.0 }
              ?.let { amount -> ParticipantAmount(it.userId, amount) }
        }
    onDone(
        if (nonZeroAmounts.size == 1) {
          val single = nonZeroAmounts.first()
          PayAmounts.OnePerson(single.userId, single.amount)
        } else {
          PayAmounts.MultiplePeople(nonZeroAmounts)
        }
    )
  }
}
