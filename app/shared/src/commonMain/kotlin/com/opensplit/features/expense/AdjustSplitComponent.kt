package com.opensplit.features.expense

import com.opensplit.component.CContext
import com.opensplit.dto.expense.SplitMethod
import com.opensplit.dto.expense.SplitType

interface AdjustSplitComponent {
  val equallyComponent: EquallySplitComponent
  val unequallyComponent: UnequallySplitComponent
  val percentageComponent: PercentageSplitComponent
  val sharesComponent: SharesSplitComponent
  val adjustmentComponent: AdjustmentSplitComponent

  fun onTabChanged(splitType: SplitType)

  fun onDoneClicked()

  interface Factory {
    fun create(
        context: CContext,
        initialParticipants: List<String>,
        totalAmount: Double,
        onDone: (SplitMethod) -> Unit,
    ): AdjustSplitComponent
  }
}

class DefaultAdjustSplitComponent(
    context: CContext,
    initialParticipants: List<String>,
    totalAmount: Double,
    private val onDone: (SplitMethod) -> Unit,
) : AdjustSplitComponent, CContext by context {

  private var currentSplitType = SplitType.EQUALLY

  override val equallyComponent = DefaultEquallySplitComponent(this, initialParticipants)
  override val unequallyComponent =
      DefaultUnequallySplitComponent(this, initialParticipants, totalAmount)
  override val percentageComponent = DefaultPercentageSplitComponent(this, initialParticipants)
  override val sharesComponent = DefaultSharesSplitComponent(this, initialParticipants)
  override val adjustmentComponent = DefaultAdjustmentSplitComponent(this, initialParticipants)

  override fun onTabChanged(splitType: SplitType) {
    currentSplitType = splitType
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
        initialParticipants: List<String>,
        totalAmount: Double,
        onDone: (SplitMethod) -> Unit,
    ): AdjustSplitComponent =
        DefaultAdjustSplitComponent(context, initialParticipants, totalAmount, onDone)
  }
}

class FakeAdjustSplitComponent(
    override val equallyComponent: EquallySplitComponent = FakeEquallySplitComponent(),
    override val unequallyComponent: UnequallySplitComponent = FakeUnequallySplitComponent(),
    override val percentageComponent: PercentageSplitComponent = FakePercentageSplitComponent(),
    override val sharesComponent: SharesSplitComponent = FakeSharesSplitComponent(),
    override val adjustmentComponent: AdjustmentSplitComponent = FakeAdjustmentSplitComponent(),
) : AdjustSplitComponent {
  override fun onTabChanged(splitType: SplitType) {}

  override fun onDoneClicked() {}
}
