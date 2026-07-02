package com.opensplit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthRootScreen
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.createjoin.CreateJoinHouseholdScreen
import com.opensplit.features.household.root.HouseholdRootScreen
import com.opensplit.features.household.root.RootHouseholdComponent
import com.opensplit.features.household.settings.HouseholdSettingsComponent
import com.opensplit.features.household.settings.HouseholdSettingsScreen
import com.opensplit.root.FakeRootComponent
import com.opensplit.root.RootComponent
import com.opensplit.splash.SplashDestination
import com.opensplit.splash.SplashScreen
import com.opensplit.ui.OpenSplitTheme

@Composable
fun App(root: RootComponent, modifier: Modifier = Modifier) {
    OpenSplitTheme {
        Children(
            modifier = modifier,
            stack = root.childStack
        ) {
            when (val child = it.instance) {
                is SplashDestination ->
                    SplashScreen()

                is AuthComponent -> AuthRootScreen(
                    component = child,
                    modifier = Modifier.fillMaxSize(),
                )

                is CreateJoinHouseholdComponent -> CreateJoinHouseholdScreen(
                    component = child,
                    modifier = Modifier.fillMaxSize(),
                )

                is RootHouseholdComponent -> HouseholdRootScreen(
                    component = child,
                    modifier = Modifier.fillMaxSize(),
                )

                is HouseholdSettingsComponent -> HouseholdSettingsScreen(
                    component = child,
                    modifier = Modifier.fillMaxSize(),
                )

                else -> error("Unknown child: $child")
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    App(FakeRootComponent())
}

