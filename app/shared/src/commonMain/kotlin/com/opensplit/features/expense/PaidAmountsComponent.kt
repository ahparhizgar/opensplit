package com.opensplit.features.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.opensplit.dto.expense.ParticipantAmount
import com.opensplit.dto.household.FakeHouseholdDtoFactory
import com.opensplit.dto.household.FakeHouseholdMemberDtoFactory
import com.opensplit.dto.household.HouseholdDto

interface PaidAmountsComponent {
  val uiState: Value<PaidAmountsUiState>

  fun onParticipantAmountChanged(userId: String, amount: String)

  fun onDone()

  interface Factory {
    fun create(
        initial: PayAmounts,
        household: HouseholdDto,
        onDone: (PayAmountsUiState) -> Unit,
    ): PaidAmountsComponent
  }
}

data class ParticipantValue(val userId: String, val value: String)

data class PaidAmountsUiState(
    val allParticipantAmounts: List<ParticipantValue>,
    val goalAmount: Double?,
)

class DefaultPaidAmountsComponent(
    initial: PayAmounts,
    household: HouseholdDto,
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
                                  initial.amount.toString(),
                              )
                          else ParticipantValue(member.userId, "")
                        }

                    is PayAmounts.MultiplePeople ->
                        household.members.map { member ->
                          initial.amounts
                              .find { it.userId == member.userId }
                              ?.let { ParticipantValue(member.userId, it.amount.toString()) }
                              ?: ParticipantValue(member.userId, "")
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
        household: HouseholdDto,
        onDone: (PayAmountsUiState) -> Unit,
    ): PaidAmountsComponent {
      return DefaultPaidAmountsComponent(initial = initial, household = household, onDone = onDone)
    }
  }
}

class FakePaidAmountsComponent(
    initial: PayAmounts =
        PayAmounts.OnePerson(FakeHouseholdMemberDtoFactory.create1().userId, 100.0),
    household: HouseholdDto = FakeHouseholdDtoFactory.create(),
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
                                  initial.amount.toString(),
                              )
                          else ParticipantValue(member.userId, "")
                        }

                    is PayAmounts.MultiplePeople ->
                        household.members.map { member ->
                          initial.amounts
                              .find { it.userId == member.userId }
                              ?.let { ParticipantValue(member.userId, it.amount.toString()) }
                              ?: ParticipantValue(member.userId, "")
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
