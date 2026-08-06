package com.opensplit.features.expense

import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.domain.Member
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SplitType
import com.opensplit.util.formatAmount

interface MoreSplitOptionsComponent {
  val equallyComponent: EquallySplitComponent
  val unequallyComponent: UnequallySplitComponent
  val percentageComponent: PercentageSplitComponent
  val sharesComponent: SharesSplitComponent
  val adjustmentComponent: AdjustmentSplitComponent
  val payerName: Value<String>
  val initialSplitType: SplitType

  fun onTabChanged(splitType: SplitType)

  fun onPayerClicked()

  fun onDoneClicked()

  interface Factory {
    fun create(
        context: CContext,
        participants: List<Member>,
        totalAmount: Double,
        initialSplitMethod: SplitMethod,
        payerName: Value<String>,
        onPayerClicked: () -> Unit,
        onDone: (SplitMethod) -> Unit,
    ): MoreSplitOptionsComponent
  }
}

class DefaultMoreSplitOptionsComponent(
    context: CContext,
    participants: List<Member>,
    totalAmount: Double,
    initialSplitMethod: SplitMethod,
    override val payerName: Value<String>,
    private val onPayerClicked: () -> Unit,
    private val onDone: (SplitMethod) -> Unit,
) : MoreSplitOptionsComponent, CContext by context {

  override val initialSplitType =
      when (initialSplitMethod) {
        is SplitMethod.Equally -> SplitType.EQUALLY
        is SplitMethod.Unequally -> SplitType.Unequally
        is SplitMethod.Percentage -> SplitType.PERCENTAGE
        is SplitMethod.Shares -> SplitType.SHARES
        is SplitMethod.Adjustment -> SplitType.ADJUSTMENT
      }

  private var currentSplitType = initialSplitType

  override val equallyComponent =
      DefaultEquallySplitComponent(
          childContext("equally"),
          participants,
          initialUserIds =
              (initialSplitMethod as? SplitMethod.Equally)?.userIds?.toSet()
                  ?: participants.map { it.userId }.toSet(),
      )
  override val unequallyComponent =
      DefaultUnequallySplitComponent(
          childContext("unequally"),
          participants,
          totalAmount,
          initialAmounts =
              (initialSplitMethod as? SplitMethod.Unequally)?.amounts?.mapValues {
                it.value.toString()
              } ?: emptyMap(),
      )
  override val percentageComponent =
      DefaultPercentageSplitComponent(
          childContext("percentage"),
          participants,
          initialPercentages =
              (initialSplitMethod as? SplitMethod.Percentage)?.percentages?.mapValues {
                it.value.toString()
              } ?: emptyMap(),
      )
  override val sharesComponent =
      DefaultSharesSplitComponent(
          childContext("shares"),
          participants,
          initialShares =
              (initialSplitMethod as? SplitMethod.Shares)?.shares?.mapValues { it.value.toString() }
                  ?: emptyMap(),
      )
  override val adjustmentComponent =
      DefaultAdjustmentSplitComponent(
          context = childContext("adjustment"),
          participants = participants,
          initialAdjustments =
              (initialSplitMethod as? SplitMethod.Adjustment)?.adjustments?.mapValues {
                it.value.formatAmount()
              } ?: emptyMap(),
      )

  override fun onTabChanged(splitType: SplitType) {
    currentSplitType = splitType
  }

  override fun onPayerClicked() {
    onPayerClicked.invoke()
  }

  override fun onDoneClicked() {
    val method =
        when (currentSplitType) {
          SplitType.EQUALLY -> equallyComponent.uiState.value.toMode()
          SplitType.Unequally ->
              SplitMethod.Unequally(
                  unequallyComponent.uiState.value.amounts.mapValues {
                    it.value.toDoubleOrNull() ?: 0.0
                  }
              )
          SplitType.PERCENTAGE ->
              SplitMethod.Percentage(
                  percentageComponent.uiState.value.percentages.mapValues {
                    it.value.toDoubleOrNull() ?: 0.0
                  }
              )
          SplitType.SHARES ->
              SplitMethod.Shares(
                  sharesComponent.uiState.value.shares.mapValues { it.value.toIntOrNull() ?: 0 }
              )
          SplitType.ADJUSTMENT ->
              SplitMethod.Adjustment(
                  adjustments =
                      adjustmentComponent.uiState.value.adjustments.mapValues {
                        it.value.toDoubleOrNull() ?: 0.0
                      },
              )
        }
    onDone(method)
  }

  class Factory : MoreSplitOptionsComponent.Factory {
    override fun create(
        context: CContext,
        participants: List<Member>,
        totalAmount: Double,
        initialSplitMethod: SplitMethod,
        payerName: Value<String>,
        onPayerClicked: () -> Unit,
        onDone: (SplitMethod) -> Unit,
    ): MoreSplitOptionsComponent =
        DefaultMoreSplitOptionsComponent(
            context = context,
            participants = participants,
            totalAmount = totalAmount,
            initialSplitMethod = initialSplitMethod,
            payerName = payerName,
            onPayerClicked = onPayerClicked,
            onDone = onDone,
        )
  }
}

class FakeMoreSplitOptionsComponent(
    override val equallyComponent: EquallySplitComponent = FakeEquallySplitComponent(),
    override val unequallyComponent: UnequallySplitComponent = FakeUnequallySplitComponent(),
    override val percentageComponent: PercentageSplitComponent = FakePercentageSplitComponent(),
    override val sharesComponent: SharesSplitComponent = FakeSharesSplitComponent(),
    override val adjustmentComponent: AdjustmentSplitComponent = FakeAdjustmentSplitComponent(),
    override val payerName: Value<String> = MutableValue("AmirHossein"),
    override val initialSplitType: SplitType = SplitType.EQUALLY,
) : MoreSplitOptionsComponent {
  override fun onTabChanged(splitType: SplitType) {}

  override fun onPayerClicked() {}

  override fun onDoneClicked() {}
}
