package com.opensplit.features.group.createjoin

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.opensplit.component.CContext
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.serialization.Serializable

interface GroupSelectionComponent {
  fun onCreateGroupClicked()

  fun onJoinGroupClicked()

  fun onBackClicked()

  @Serializable class Config : TopLevelDestinationConfig
}

interface GroupSelectionComponentFactory {
  fun create(cContext: CContext): GroupSelectionComponent
}

class DefaultGroupSelectionComponent(
    context: CContext,
) : GroupSelectionComponent, CContext by context {

  override fun onCreateGroupClicked() {
    navigation.pushNew(CreateGroupComponent.Config())
  }

  override fun onJoinGroupClicked() {
    navigation.pushNew(JoinGroupComponent.Config())
  }

  override fun onBackClicked() {
    navigation.pop()
  }
}

class DefaultGroupSelectionComponentFactory : GroupSelectionComponentFactory {
  override fun create(cContext: CContext): GroupSelectionComponent =
      DefaultGroupSelectionComponent(cContext)
}

class FakeGroupSelectionComponent : GroupSelectionComponent {
  override fun onCreateGroupClicked() {}

  override fun onJoinGroupClicked() {}

  override fun onBackClicked() {}
}
