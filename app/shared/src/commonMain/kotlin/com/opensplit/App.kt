package com.opensplit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.createjoin.CreateJoinHouseholdScreen
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.features.household.details.HouseholdDetailsScreen
import com.opensplit.features.household.my.MyHouseholdsListComponent
import com.opensplit.features.household.my.MyHouseholdsListScreen
import com.opensplit.features.household.settings.HouseholdSettingsComponent
import com.opensplit.features.household.settings.HouseholdSettingsScreen
import com.opensplit.root.FakeRootComponent
import com.opensplit.root.RootComponent
import com.opensplit.splash.SplashDestination
import com.opensplit.splash.SplashScreen
import com.opensplit.ui.OpenSplitTheme
import com.opensplit.usermessage.CustomSnackbar

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun App(root: RootComponent, modifier: Modifier = Modifier) {
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

        is CreateJoinHouseholdComponent -> {
          CreateJoinHouseholdScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is HouseholdSettingsComponent -> {
          HouseholdSettingsScreen(
              component = child,
              modifier = Modifier.fillMaxSize(),
          )
        }

        is MyHouseholdsListComponent -> {
          MyHouseholdsListScreen(
              modifier = Modifier.fillMaxSize().testTag("household-list"),
              component = child,
          )
        }

        is HouseholdDetailsComponent -> {
          HouseholdDetailsScreen(
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
