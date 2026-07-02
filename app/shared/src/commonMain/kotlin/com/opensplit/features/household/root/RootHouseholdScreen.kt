package com.opensplit.features.household.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.opensplit.features.household.details.HouseholdDetailsScreen
import com.opensplit.features.household.my.MyHouseholdsListScreen
import com.opensplit.features.household.createjoin.CreateJoinHouseholdScreen
import com.opensplit.features.household.settings.HouseholdSettingsScreen

@Composable
fun HouseholdRootScreen(
    component: RootHouseholdComponent,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = component.childStack,
        modifier = modifier,
    ) {
        when (val child = it.instance) {
            is RootHouseholdComponent.Child.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is RootHouseholdComponent.Child.List ->
                MyHouseholdsListScreen(
                    component = child.component,
                    onHouseholdClick = { id ->
                        component.onHouseholdClick(id)
                    }
                )

            is RootHouseholdComponent.Child.Detail ->
                HouseholdDetailsScreen(
                    component = child.component,
                )

            is RootHouseholdComponent.Child.Settings ->
                HouseholdSettingsScreen(child.component)
        }
    }
}
