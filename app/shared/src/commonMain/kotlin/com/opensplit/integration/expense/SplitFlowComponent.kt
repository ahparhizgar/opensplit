package com.opensplit.integration.expense

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.opensplit.component.CContext
import com.opensplit.dto.expense.SplitMethod
import kotlinx.serialization.Serializable

interface SplitFlowComponent {
  val stack: Value<ChildStack<*, Child>>

  fun onBackClicked()

  sealed class Child {
    class QuickSplit(val component: QuickSplitComponent) : Child()

    class MoreSplitOptions(val component: MoreSplitOptionsComponent) : Child()
  }
}

interface SplitFlowComponentFactory {
  fun create(
      context: CContext,
      groupId: String,
      parentUiState: Value<AddExpenseUiState>,
      onNavigateToPayerFlow: () -> Unit,
      onQuickSplitDone: (PayAmountsUiState, SplitMethod) -> Unit,
      onMoreSplitDone: (SplitMethod) -> Unit,
      onFinished: () -> Unit,
  ): SplitFlowComponent
}

@Serializable
sealed class SplitFlowConfig {
  @Serializable data object QuickSplit : SplitFlowConfig()

  @Serializable data object MoreSplitOptions : SplitFlowConfig()
}

class DefaultSplitFlowComponent(
    context: CContext,
    groupId: String,
    parentUiState: Value<AddExpenseUiState>,
    quickSplitComponentFactory: QuickSplitComponentFactory,
    moreSplitOptionsComponentFactory: MoreSplitOptionsComponentFactory,
    private val onNavigateToPayerFlow: () -> Unit,
    private val onQuickSplitDone: (PayAmountsUiState, SplitMethod) -> Unit,
    private val onMoreSplitDone: (SplitMethod) -> Unit,
    private val onFinished: () -> Unit,
) : SplitFlowComponent, CContext by context {

  private val stackNavigation = StackNavigation<SplitFlowConfig>()

  override fun onBackClicked() {
    if (stack.value.items.size > 1) {
      stackNavigation.pop()
    } else {
      onFinished()
    }
  }

  override val stack: Value<ChildStack<*, SplitFlowComponent.Child>> =
      childStack(
          source = stackNavigation,
          serializer = SplitFlowConfig.serializer(),
          initialConfiguration = SplitFlowConfig.QuickSplit,
          handleBackButton = true,
          childFactory = { config, componentContext ->
            when (config) {
              is SplitFlowConfig.QuickSplit -> {
                SplitFlowComponent.Child.QuickSplit(
                    quickSplitComponentFactory.create(
                        context = componentContext,
                        parentUiState = parentUiState,
                        groupId = groupId,
                        onOptionSelected = { amounts, method -> onQuickSplitDone(amounts, method) },
                        onAdjustSplitClicked = {
                          stackNavigation.pushNew(SplitFlowConfig.MoreSplitOptions)
                        },
                    )
                )
              }
              is SplitFlowConfig.MoreSplitOptions ->
                  SplitFlowComponent.Child.MoreSplitOptions(
                      moreSplitOptionsComponentFactory.create(
                          context = componentContext,
                          participants = parentUiState.value.participants,
                          totalAmount = parentUiState.value.payAmountsDomain.sum(),
                          initialSplitMethod = parentUiState.value.splitMethod,
                          payerName =
                              parentUiState.map { state ->
                                when (state.payAmountsDomain) {
                                  is PayAmounts.MultiplePeople -> "Multiple people"
                                  is PayAmounts.OnePerson ->
                                      state.getParticipantName(state.payAmountsDomain.userId)
                                }
                              },
                          onPayerClicked = onNavigateToPayerFlow,
                          onDone = { splitMethod -> onMoreSplitDone(splitMethod) },
                      )
                  )
            }
          },
      )
}

class DefaultSplitFlowComponentFactory(
    private val quickSplitComponentFactory: QuickSplitComponentFactory,
    private val moreSplitOptionsComponentFactory: MoreSplitOptionsComponentFactory,
) : SplitFlowComponentFactory {
  override fun create(
      context: CContext,
      groupId: String,
      parentUiState: Value<AddExpenseUiState>,
      onNavigateToPayerFlow: () -> Unit,
      onQuickSplitDone: (PayAmountsUiState, SplitMethod) -> Unit,
      onMoreSplitDone: (SplitMethod) -> Unit,
      onFinished: () -> Unit,
  ): SplitFlowComponent =
      DefaultSplitFlowComponent(
          context = context,
          groupId = groupId,
          parentUiState = parentUiState,
          quickSplitComponentFactory = quickSplitComponentFactory,
          moreSplitOptionsComponentFactory = moreSplitOptionsComponentFactory,
          onNavigateToPayerFlow = onNavigateToPayerFlow,
          onQuickSplitDone = onQuickSplitDone,
          onMoreSplitDone = onMoreSplitDone,
          onFinished = onFinished,
      )
}

class FakeSplitFlowComponent(
    childFactory: (SplitFlowComponent) -> SplitFlowComponent.Child = {
      SplitFlowComponent.Child.QuickSplit(FakeQuickSplitComponent())
    }
) : SplitFlowComponent {
  override val stack: Value<ChildStack<*, SplitFlowComponent.Child>> =
      MutableValue(ChildStack(configuration = Unit, instance = childFactory(this)))

  override fun onBackClicked() {}
}
