package com.opensplit.features.household.createjoin

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.opensplit.component.CContext
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.serialization.Serializable

interface HouseholdSelectionComponent {
  fun onCreateHouseholdClicked()
  fun onJoinHouseholdClicked()
  fun onBackClicked()

  @Serializable class Config : TopLevelDestinationConfig
}

interface HouseholdSelectionComponentFactory {
  fun create(cContext: CContext): HouseholdSelectionComponent
}

class DefaultHouseholdSelectionComponent(
    context: CContext,
) : HouseholdSelectionComponent, CContext by context {

  override fun onCreateHouseholdClicked() {
    navigation.pushNew(CreateHouseholdComponent.Config())
  }

  override fun onJoinHouseholdClicked() {
    navigation.pushNew(JoinHouseholdComponent.Config())
  }

  override fun onBackClicked() {
    navigation.pop()
  }
}

class DefaultHouseholdSelectionComponentFactory : HouseholdSelectionComponentFactory {
  override fun create(cContext: CContext): HouseholdSelectionComponent =
      DefaultHouseholdSelectionComponent(cContext)
}

class FakeHouseholdSelectionComponent : HouseholdSelectionComponent {
  override fun onCreateHouseholdClicked() {}
  override fun onJoinHouseholdClicked() {}
  override fun onBackClicked() {}
}
