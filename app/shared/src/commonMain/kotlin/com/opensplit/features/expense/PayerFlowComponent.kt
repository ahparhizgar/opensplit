package com.opensplit.features.expense

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import kotlinx.serialization.Serializable

interface PayerFlowComponent {
  val stack: Value<ChildStack<*, Child>>

  fun onBackClicked()

  sealed class Child {
    class WhoPaid(val component: WhoPaidComponent) : Child()

    class PaidAmounts(val component: PaidAmountsComponent) : Child()
  }
}

interface PayerFlowComponentFactory {
  fun create(
      context: CContext,
      parentUiState: Value<AddExpenseUiState>,
      onPayerSelected: (String) -> Unit,
      onPaidAmountsDone: (PayAmountsUiState) -> Unit,
      onFinished: () -> Unit,
  ): PayerFlowComponent
}

@Serializable
sealed class PayerFlowConfig {
  @Serializable data object WhoPaid : PayerFlowConfig()

  @Serializable data object PaidAmounts : PayerFlowConfig()
}

class DefaultPayerFlowComponent(
    context: CContext,
    parentUiState: Value<AddExpenseUiState>,
    whoPaidComponentFactory: WhoPaidComponentFactory,
    paidAmountsComponentFactory: PaidAmountsComponentFactory,
    private val onPayerSelected: (String) -> Unit,
    private val onPaidAmountsDone: (PayAmountsUiState) -> Unit,
    private val onFinished: () -> Unit,
) : PayerFlowComponent, CContext by context {

  private val stackNavigation = StackNavigation<PayerFlowConfig>()

  override fun onBackClicked() {
    if (stack.value.items.size > 1) {
      stackNavigation.pop()
    } else {
      onFinished()
    }
  }

  override val stack: Value<ChildStack<*, PayerFlowComponent.Child>> =
      childStack(
          source = stackNavigation,
          serializer = PayerFlowConfig.serializer(),
          initialConfiguration = PayerFlowConfig.WhoPaid,
          handleBackButton = true,
          childFactory = { config, componentContext ->
            when (config) {
              is PayerFlowConfig.WhoPaid ->
                  PayerFlowComponent.Child.WhoPaid(
                      whoPaidComponentFactory.create(
                          context = componentContext,
                          participants = parentUiState.value.participants,
                          selectedUserId =
                              (parentUiState.value.payAmounts as? PayAmountsUiState.OnePerson)
                                  ?.userId,
                          onParticipantSelected = { userId -> onPayerSelected(userId) },
                          onMultiplePeopleClicked = {
                            stackNavigation.pushNew(PayerFlowConfig.PaidAmounts)
                          },
                      )
                  )
              is PayerFlowConfig.PaidAmounts ->
                  PayerFlowComponent.Child.PaidAmounts(
                      paidAmountsComponentFactory.create(
                          parentUiState = parentUiState,
                          onDone = { amounts -> onPaidAmountsDone(amounts) },
                          cContext = componentContext,
                      )
                  )
            }
          },
      )
}

class DefaultPayerFlowComponentFactory(
    private val whoPaidComponentFactory: WhoPaidComponentFactory,
    private val paidAmountsComponentFactory: PaidAmountsComponentFactory,
) : PayerFlowComponentFactory {
  override fun create(
      context: CContext,
      parentUiState: Value<AddExpenseUiState>,
      onPayerSelected: (String) -> Unit,
      onPaidAmountsDone: (PayAmountsUiState) -> Unit,
      onFinished: () -> Unit,
  ): PayerFlowComponent =
      DefaultPayerFlowComponent(
          context = context,
          parentUiState = parentUiState,
          whoPaidComponentFactory = whoPaidComponentFactory,
          paidAmountsComponentFactory = paidAmountsComponentFactory,
          onPayerSelected = onPayerSelected,
          onPaidAmountsDone = onPaidAmountsDone,
          onFinished = onFinished,
      )
}

class FakePayerFlowComponent(
    childFactory: (PayerFlowComponent) -> PayerFlowComponent.Child = {
      PayerFlowComponent.Child.WhoPaid(FakeWhoPaidComponent())
    }
) : PayerFlowComponent {
  override val stack: Value<ChildStack<*, PayerFlowComponent.Child>> =
      MutableValue(ChildStack(configuration = Unit, instance = childFactory(this)))

  override fun onBackClicked() {}
}
