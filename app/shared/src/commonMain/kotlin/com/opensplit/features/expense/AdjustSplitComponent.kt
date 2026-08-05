package com.opensplit.features.expense

import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.domain.Member
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SplitType

interface AdjustSplitComponent {
  val equallyComponent: EquallySplitComponent
  val unequallyComponent: UnequallySplitComponent
  val percentageComponent: PercentageSplitComponent
  val sharesComponent: SharesSplitComponent
  val adjustmentComponent: AdjustmentSplitComponent
  val payerName: Value<String>

  fun onTabChanged(splitType: SplitType)

  fun onPayerClicked()

  fun onDoneClicked()

  interface Factory {
    fun create(
        context: CContext,
        participants: List<Member>,
        totalAmount: Double,
        payerName: Value<String>,
        onPayerClicked: () -> Unit,
        onDone: (SplitMethod) -> Unit,
    ): AdjustSplitComponent
  }
}

class DefaultAdjustSplitComponent(
    context: CContext,
    participants: List<Member>,
    totalAmount: Double,
    override val payerName: Value<String>,
    private val onPayerClicked: () -> Unit,
    private val onDone: (SplitMethod) -> Unit,
) : AdjustSplitComponent, CContext by context {

  private var currentSplitType = SplitType.EQUALLY

  override val equallyComponent =
      DefaultEquallySplitComponent(childContext("equally"), participants)
  override val unequallyComponent =
      DefaultUnequallySplitComponent(childContext("unequally"), participants, totalAmount)
  override val percentageComponent =
      DefaultPercentageSplitComponent(childContext("percentage"), participants)
  override val sharesComponent = DefaultSharesSplitComponent(childContext("shares"), participants)
  override val adjustmentComponent =
      DefaultAdjustmentSplitComponent(childContext("adjustment"), participants)

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

  class Factory : AdjustSplitComponent.Factory {
    override fun create(
        context: CContext,
        participants: List<Member>,
        totalAmount: Double,
        payerName: Value<String>,
        onPayerClicked: () -> Unit,
        onDone: (SplitMethod) -> Unit,
    ): AdjustSplitComponent =
        DefaultAdjustSplitComponent(
            context,
            participants,
            totalAmount,
            payerName,
            onPayerClicked,
            onDone,
        )
  }
}

class FakeAdjustSplitComponent(
    override val equallyComponent: EquallySplitComponent = FakeEquallySplitComponent(),
    override val unequallyComponent: UnequallySplitComponent = FakeUnequallySplitComponent(),
    override val percentageComponent: PercentageSplitComponent = FakePercentageSplitComponent(),
    override val sharesComponent: SharesSplitComponent = FakeSharesSplitComponent(),
    override val adjustmentComponent: AdjustmentSplitComponent = FakeAdjustmentSplitComponent(),
    override val payerName: Value<String> = MutableValue("AmirHossein"),
) : AdjustSplitComponent {
  override fun onTabChanged(splitType: SplitType) {}

  override fun onPayerClicked() {}

  override fun onDoneClicked() {}
}
