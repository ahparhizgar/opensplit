package com.opensplit.integration.expense

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanels
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanelsAnimators
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.materialPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.panels.ChildPanelsMode

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun AddExpenseFlowScreen(
    component: AddExpenseFlowComponent,
    modifier: Modifier = Modifier,
) {
  val panels by component.panels.subscribeAsState()
  val isDualPane = panels.mode == ChildPanelsMode.DUAL

  ChildPanels(
      modifier = modifier,
      panels = component.panels,
      mainChild = { child ->
        AddExpenseScreen(
            component = child.instance,
            modifier = Modifier.fillMaxSize(),
        )
      },
      detailsChild = { child ->
        when (val instance = child.instance) {
          is AddExpenseFlowComponent.DetailsChild.PayerFlow -> {
            PayerFlowScreen(
                component = instance.component,
                isDualPane = isDualPane,
                modifier = Modifier.fillMaxSize(),
            )
          }

          is AddExpenseFlowComponent.DetailsChild.SplitFlow -> {
            SplitFlowScreen(
                component = instance.component,
                isDualPane = isDualPane,
                modifier = Modifier.fillMaxSize(),
            )
          }
        }
      },
      secondPanelPlaceholder = {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Select who paid or how to split.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      },
      animators =
          remember {
            ChildPanelsAnimators(
                main = { _, _, _, _ -> fade() + scale() },
                details = { _, _, _, _ -> fade() + scale() },
            )
          },
      predictiveBackParams = {
        PredictiveBackParams(
            backHandler = component.backHandler,
            onBack = component::onBack,
            animatable = ::materialPredictiveBackAnimatable,
        )
      },
  )
}
