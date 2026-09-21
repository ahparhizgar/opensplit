package com.opensplit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthRootScreen
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.AddExpenseScreen
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.expense.ExpenseDetailsScreen
import com.opensplit.features.group.createjoin.CreateGroupComponent
import com.opensplit.features.group.createjoin.CreateGroupScreen
import com.opensplit.features.group.createjoin.GroupSelectionComponent
import com.opensplit.features.group.createjoin.GroupSelectionScreen
import com.opensplit.features.group.createjoin.JoinGroupComponent
import com.opensplit.features.group.createjoin.JoinGroupScreen
import com.opensplit.features.group.details.GroupDetailsComponent
import com.opensplit.features.group.details.GroupDetailsScreen
import com.opensplit.features.group.details.GroupFlowComponent
import com.opensplit.features.group.details.GroupFlowScreen
import com.opensplit.features.group.my.MyGroupsListComponent
import com.opensplit.features.group.my.MyGroupsListScreen
import com.opensplit.features.group.settings.GroupSettingsComponent
import com.opensplit.features.group.settings.GroupSettingsScreen
import com.opensplit.features.profile.ProfileComponent
import com.opensplit.features.profile.ProfileScreen
import com.opensplit.root.FakeRootComponent
import com.opensplit.root.RootComponent
import com.opensplit.splash.SplashDestination
import com.opensplit.splash.SplashScreen
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.usermessage.CustomSnackbar

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App(root: RootComponent, modifier: Modifier = Modifier) {
  val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
  LaunchedEffect(windowSizeClass) { root.windowSizeHolder.update(windowSizeClass) }

  OpenSplitTheme {
    val hostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
      while (true) {
        root.messageHolder.showAll { hostState.showSnackbar(it) }
      }
    }

    Children(
        // The background is to prevent a flicker when animating children
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        stack = root.childStack,
        animation =
            predictiveBackAnimation(
                backHandler = root.backHandler,
                onBack = root::onBack,
                fallbackAnimation = stackAnimation(fade() + scale()),
            ),
    ) {
      when (val child = it.instance) {
        is SplashDestination -> {
          SplashScreen(modifier = Modifier.testTag("splash-screen"))
        }

        is AuthComponent -> {
          AuthRootScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is GroupSelectionComponent -> {
          GroupSelectionScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is CreateGroupComponent -> {
          CreateGroupScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is JoinGroupComponent -> {
          JoinGroupScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is GroupSettingsComponent -> {
          GroupSettingsScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is MyGroupsListComponent -> {
          MyGroupsListScreen(
              modifier = Modifier.fillMaxSize().testTag("group-list"),
              component = child,
          )
        }

        is GroupFlowComponent -> {
          GroupFlowScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is GroupDetailsComponent -> {
          GroupDetailsScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is AddExpenseComponent -> {
          AddExpenseScreen(
              component = child,
          )
        }

        is ExpenseDetailsComponent -> {
          ExpenseDetailsScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is ProfileComponent -> {
          ProfileScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        else -> {
          error("Unknown child: $child")
        }
      }
    }
    SnackbarHost(hostState = hostState) { CustomSnackbar(snackbarData = it) }
  }
}

@Preview
@Composable
private fun Preview() {
  App(FakeRootComponent())
}
