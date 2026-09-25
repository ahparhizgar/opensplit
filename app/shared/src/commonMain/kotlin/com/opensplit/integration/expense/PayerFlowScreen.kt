package com.opensplit.integration.expense

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun PayerFlowScreen(
    component: PayerFlowComponent,
    isDualPane: Boolean,
    modifier: Modifier = Modifier,
) {
  val stack by component.stack.subscribeAsState()
  val showBackButton = stack.items.size > 1 || !isDualPane

  Children(stack = component.stack, modifier = modifier.fillMaxSize()) { child ->
    when (val instance = child.instance) {
      is PayerFlowComponent.Child.WhoPaid ->
          WhoPaidScreen(
              component = instance.component,
              showBackButton = showBackButton,
              onBackClicked = component::onBackClicked,
          )
      is PayerFlowComponent.Child.PaidAmounts ->
          PaidAmountsScreen(
              component = instance.component,
              showBackButton = showBackButton,
              onBackClicked = component::onBackClicked,
          )
    }
  }
}
