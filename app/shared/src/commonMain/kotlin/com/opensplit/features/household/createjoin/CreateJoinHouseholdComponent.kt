package com.opensplit.features.household.createjoin

import com.arkivanov.decompose.router.stack.replaceCurrent
import com.opensplit.component.CContext
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.repository.HouseholdRepository
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

enum class HouseholdTab {
  Create,
  Join,
}

interface CreateJoinHouseholdComponent {
  val activeTab: StateFlow<HouseholdTab>
  val createComponent: CreateHouseholdComponent
  val joinComponent: JoinHouseholdComponent

  fun useCreate()

  fun useJoin()

  @Serializable class Config : TopLevelDestinationConfig

  interface Factory {
    fun create(cContext: CContext): CreateJoinHouseholdComponent
  }
}

class DefaultCreateJoinHouseholdComponent(
    context: CContext,
    householdRepository: HouseholdRepository,
) : CreateJoinHouseholdComponent, CContext by context {

  override val createComponent: CreateHouseholdComponent =
      DefaultCreateHouseholdComponent(
          context = context,
          householdRepository = householdRepository,
          onDone = { household ->
            navigation.replaceCurrent(HouseholdDetailsComponent.Config(household.id))
          },
      )
  override val joinComponent: JoinHouseholdComponent =
      DefaultJoinHouseholdComponent(context, householdRepository)

  private val _activeTab = MutableStateFlow(HouseholdTab.Create)
  override val activeTab: StateFlow<HouseholdTab> = _activeTab

  override fun useCreate() {
    _activeTab.value = HouseholdTab.Create
  }

  override fun useJoin() {
    _activeTab.value = HouseholdTab.Join
  }

  class Factory(
      private val householdRepository: HouseholdRepository,
  ) : CreateJoinHouseholdComponent.Factory {
    override fun create(cContext: CContext): CreateJoinHouseholdComponent =
        DefaultCreateJoinHouseholdComponent(cContext, householdRepository)
  }
}

class FakeCreateJoinHouseholdComponent(
    override val activeTab: MutableStateFlow<HouseholdTab> = MutableStateFlow(HouseholdTab.Create),
    override val createComponent: CreateHouseholdComponent = FakeCreateHouseholdComponent(),
    override val joinComponent: JoinHouseholdComponent = FakeJoinHouseholdComponent(),
) : CreateJoinHouseholdComponent {
  override fun useCreate() {
    activeTab.update { HouseholdTab.Create }
  }

  override fun useJoin() {
    activeTab.update { HouseholdTab.Join }
  }
}
