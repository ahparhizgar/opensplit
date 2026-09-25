package com.opensplit.integration.expense

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.decompose.value.update
import com.opensplit.component.CContext
import com.opensplit.domain.FakeMemberFactory
import com.opensplit.domain.Member
import com.opensplit.dto.expense.ParticipantAmount

interface PaidAmountsComponent {
  val uiState: Value<PaidAmountsUiState>

  fun onParticipantAmountChanged(userId: String, amount: String)

  fun onDone()
}

interface PaidAmountsComponentFactory {
  fun create(
      parentUiState: Value<AddExpenseUiState>,
      onDone: (PayAmountsUiState) -> Unit,
      cContext: CContext,
  ): PaidAmountsComponent
}

data class ParticipantValue(val userId: String, val name: String, val value: String)

data class PaidAmountsUiState(
    val allParticipantAmounts: List<ParticipantValue>,
    val goalAmount: Double?,
)

class DefaultPaidAmountsComponent(
    private val parentUiState: Value<AddExpenseUiState>,
    private val onDone: (PayAmountsUiState) -> Unit,
    cContext: CContext,
) : PaidAmountsComponent, CContext by cContext {

  private val _uiState = MutableValue(createState(parentUiState.value))
  override val uiState: Value<PaidAmountsUiState> = _uiState

  init {
    parentUiState.subscribe(lifecycle) { parentState ->
      _uiState.update {
        it.copy(
            goalAmount =
                when (val amounts = parentState.payAmountsDomain) {
                  is PayAmounts.OnePerson -> amounts.amount?.takeIf { it > 0.0 }
                  is PayAmounts.MultiplePeople -> null
                }
        )
      }
    }
  }

  private fun createState(parentState: AddExpenseUiState): PaidAmountsUiState {
    val initial = parentState.payAmountsDomain
    val members = parentState.participants
    return PaidAmountsUiState(
        goalAmount =
            when (initial) {
              is PayAmounts.OnePerson -> initial.amount?.takeIf { it > 0.0 }
              is PayAmounts.MultiplePeople -> null
            },
        allParticipantAmounts =
            when (initial) {
              is PayAmounts.OnePerson ->
                  members.map { member ->
                    if (member.userId == initial.userId)
                        ParticipantValue(
                            initial.userId,
                            member.name,
                            initial.amount?.toString().orEmpty(),
                        )
                    else ParticipantValue(member.userId, member.name, "")
                  }

              is PayAmounts.MultiplePeople ->
                  members.map { member ->
                    initial.amounts
                        .find { it.userId == member.userId }
                        ?.let { ParticipantValue(member.userId, member.name, it.amount.toString()) }
                        ?: ParticipantValue(member.userId, member.name, "")
                  }
            },
    )
  }

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
}

class DefaultPaidAmountsComponentFactory : PaidAmountsComponentFactory {
  override fun create(
      parentUiState: Value<AddExpenseUiState>,
      onDone: (PayAmountsUiState) -> Unit,
      cContext: CContext,
  ): PaidAmountsComponent {
    return DefaultPaidAmountsComponent(parentUiState = parentUiState, onDone = onDone, cContext)
  }
}

class FakePaidAmountsComponent(
    initial: PayAmounts = PayAmounts.OnePerson("user-1", 100.0),
    members: List<Member> =
        listOf(FakeMemberFactory.create(userId = "user-1", isCurrentUser = true)),
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
                        members.map { member ->
                          if (member.userId == initial.userId)
                              ParticipantValue(
                                  initial.userId,
                                  member.name,
                                  initial.amount?.toString().orEmpty(),
                              )
                          else ParticipantValue(member.userId, member.name, "")
                        }

                    is PayAmounts.MultiplePeople ->
                        members.map { member ->
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
