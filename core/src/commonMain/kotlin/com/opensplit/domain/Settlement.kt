package com.opensplit.domain

import kotlin.math.abs
import kotlin.math.min
import kotlinx.serialization.Serializable

/**
 * Net balance of a single participant in a group.
 *
 * Positive means the participant paid more than they consumed and should get money back, negative
 * means they still owe money. This mirrors the `balance` already stored per member (see
 * `Member.balance`), which is accumulated as `paidShare - consumedShare` per expense.
 */
@Serializable
data class ParticipantBalance(
    val userId: String,
    val balance: Double,
) {
  val isSettled: Boolean
    get() = abs(balance) < SettlementCalculator.EPSILON
}

/** One simplified settle-up payment: [fromUserId] pays [amount] to [toUserId]. */
@Serializable
data class SettlementTransfer(
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
)

/**
 * The full settlement state of a group: every participant's net balance plus the simplified list of
 * transfers that zeroes all balances.
 */
@Serializable
data class GroupSettlement(
    val groupId: String,
    val balances: List<ParticipantBalance>,
    val transfers: List<SettlementTransfer>,
) {
  val isSettled: Boolean
    get() = transfers.isEmpty()
}

/**
 * Computes group balances and simplified settle-up transfers.
 *
 * Balances are derived from the same per-expense shares the rest of the app already uses
 * (`ParticipantShare.paidShare - ParticipantShare.consumedShare`), so this needs no schema or sync
 * changes: it is a pure calculation over existing [Expense] / [Member] data.
 */
object SettlementCalculator {

  /** Amounts below half a cent are treated as zero; amounts in the app are [Double]s. */
  const val EPSILON: Double = 0.005

  /** Net balance per participant, aggregated over all given [expenses]. */
  fun balancesFromExpenses(expenses: List<Expense>): List<ParticipantBalance> {
    val balances = linkedMapOf<String, Double>()
    expenses.forEach { expense ->
      expense.participants.forEach { participant ->
        balances[participant.userId] =
            (balances[participant.userId] ?: 0.0) + participant.netBalance
      }
    }
    return balances.map { (userId, balance) -> ParticipantBalance(userId, balance) }
  }

  /** Net balances already stored on the group's [members]. */
  fun balancesFromMembers(members: List<Member>): List<ParticipantBalance> =
      members.map { ParticipantBalance(it.userId, it.balance) }

  /**
   * Settlement for a [group] based on its [expenses]. Every group member appears in the result,
   * even with a zero balance, and participants who are no longer members are kept (sorted by
   * userId, after the members) so their debt is not silently dropped.
   */
  fun forGroup(group: Group, expenses: List<Expense>): GroupSettlement {
    val expenseBalances = balancesFromExpenses(expenses).associate { it.userId to it.balance }
    val memberIds = group.members.map { it.userId }
    val balances =
        memberIds.map { ParticipantBalance(it, expenseBalances[it] ?: 0.0) } +
            expenseBalances.keys
                .filter { it !in memberIds }
                .sorted()
                .map { ParticipantBalance(it, expenseBalances.getValue(it)) }
    return GroupSettlement(
        groupId = group.id,
        balances = balances,
        transfers = settleUp(balances),
    )
  }

  /** Settlement for a group identified only by [groupId], based on its [expenses]. */
  fun forExpenses(groupId: String, expenses: List<Expense>): GroupSettlement {
    val balances = balancesFromExpenses(expenses)
    return GroupSettlement(
        groupId = groupId,
        balances = balances,
        transfers = settleUp(balances),
    )
  }

  /**
   * Simplified transfers that zero all [balances]: the largest debtor pays the largest creditor
   * first (greedy), which needs at most `participants - 1` transfers. Ordering is deterministic: by
   * amount, then by userId. Duplicate userIds are aggregated first.
   */
  fun settleUp(balances: List<ParticipantBalance>): List<SettlementTransfer> {
    val aggregated =
        balances.groupBy { it.userId }.mapValues { (_, entries) -> entries.sumOf { it.balance } }

    val debtors =
        aggregated.entries
            .filter { it.value < -EPSILON }
            .sortedWith(compareBy({ it.value }, { it.key }))
            .map { it.key to -it.value }
            .toMutableList()
    val creditors =
        aggregated.entries
            .filter { it.value > EPSILON }
            .sortedWith(
                compareByDescending<Map.Entry<String, Double>> { it.value }.thenBy { it.key }
            )
            .map { it.key to it.value }
            .toMutableList()

    val transfers = mutableListOf<SettlementTransfer>()
    var debtorIndex = 0
    var creditorIndex = 0
    while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
      val (debtorId, debt) = debtors[debtorIndex]
      val (creditorId, credit) = creditors[creditorIndex]
      val amount = min(debt, credit)
      if (amount > EPSILON) {
        transfers +=
            SettlementTransfer(fromUserId = debtorId, toUserId = creditorId, amount = amount)
      }
      val remainingDebt = debt - amount
      val remainingCredit = credit - amount
      if (remainingDebt <= EPSILON) {
        debtorIndex++
      } else {
        debtors[debtorIndex] = debtorId to remainingDebt
      }
      if (remainingCredit <= EPSILON) {
        creditorIndex++
      } else {
        creditors[creditorIndex] = creditorId to remainingCredit
      }
    }
    return transfers
  }
}

object FakeParticipantBalanceFactory {
  fun create(
      userId: String = "user-1",
      balance: Double = 0.0,
  ) = ParticipantBalance(userId = userId, balance = balance)

  fun createList() =
      listOf(
          create(userId = "user-1", balance = 10.0),
          create(userId = "user-2", balance = -10.0),
      )
}

object FakeSettlementTransferFactory {
  fun create(
      fromUserId: String = "user-2",
      toUserId: String = "user-1",
      amount: Double = 10.0,
  ) = SettlementTransfer(fromUserId = fromUserId, toUserId = toUserId, amount = amount)

  fun createList() = listOf(create())
}

object FakeGroupSettlementFactory {
  fun create(
      groupId: String = "group-1",
      balances: List<ParticipantBalance> = FakeParticipantBalanceFactory.createList(),
      transfers: List<SettlementTransfer> = FakeSettlementTransferFactory.createList(),
  ) = GroupSettlement(groupId = groupId, balances = balances, transfers = transfers)
}
