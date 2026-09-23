package com.opensplit.features.expense

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitFlowScreen(
    component: SplitFlowComponent,
    isDualPane: Boolean,
    modifier: Modifier = Modifier,
) {
  val stack by component.stack.subscribeAsState()
  val activeChild = stack.active.instance

  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = {
              Text(
                  when (activeChild) {
                    is SplitFlowComponent.Child.QuickSplit -> "How was this expense split?"
                    is SplitFlowComponent.Child.MoreSplitOptions -> "Adjust split"
                  }
              )
            },
            navigationIcon = {
              val showBackButton = stack.items.size > 1 || !isDualPane
              if (showBackButton) {
                IconButton(onClick = component::onBackClicked) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
              }
            },
            actions = {
              if (activeChild is SplitFlowComponent.Child.MoreSplitOptions) {
                IconButton(onClick = { activeChild.component.onDoneClicked() }) {
                  Icon(Icons.Default.Check, contentDescription = "Done")
                }
              }
            },
        )
      },
  ) { padding ->
    Children(stack = component.stack, modifier = Modifier.fillMaxSize().padding(padding)) { child ->
      when (val instance = child.instance) {
        is SplitFlowComponent.Child.QuickSplit -> QuickSplitScreen(instance.component)
        is SplitFlowComponent.Child.MoreSplitOptions -> MoreSplitOptionsScreen(instance.component)
      }
    }
  }
}
