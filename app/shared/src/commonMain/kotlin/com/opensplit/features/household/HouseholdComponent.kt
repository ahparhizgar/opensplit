package com.opensplit.features.household

import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.root.Destination
import com.opensplit.root.DestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

enum class HouseholdTab { Create, Join }

interface HouseholdComponent : Destination {
    val activeTab: StateFlow<HouseholdTab>
    val createComponent: CreateHouseholdComponent
    val joinComponent: JoinHouseholdComponent
    val householdId: StateFlow<String?>
    fun useCreate()
    fun useJoin()

    @Serializable
    class Config : DestinationConfig {
        override val componentClass: KClass<out Any> = HouseholdComponent::class
    }
}

class DefaultHouseholdComponent(
    context: CContext,
    gateway: HouseholdGateway,
) : HouseholdComponent, CContext by context {

    private val scope = componentScope()

    override val createComponent: CreateHouseholdComponent = DefaultCreateHouseholdComponent(context, gateway)
    override val joinComponent: JoinHouseholdComponent = DefaultJoinHouseholdComponent(context, gateway)

    private val _activeTab = MutableStateFlow(HouseholdTab.Create)
    override val activeTab: StateFlow<HouseholdTab> = _activeTab

    override val householdId: StateFlow<String?> = combine(
        createComponent.uiState.map { it.householdId },
        joinComponent.uiState.map { it.householdId },
    ) { createId, joinId -> createId ?: joinId }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    override fun useCreate() {
        _activeTab.value = HouseholdTab.Create
    }

    override fun useJoin() {
        _activeTab.value = HouseholdTab.Join
    }
}

class FakeHouseholdComponent(
    householdId: String? = "household-1",
) : HouseholdComponent {
    override val createComponent: CreateHouseholdComponent = FakeCreateHouseholdComponent()
    override val joinComponent: JoinHouseholdComponent = FakeJoinHouseholdComponent()
    private val _activeTab = MutableStateFlow(HouseholdTab.Create)
    override val activeTab: StateFlow<HouseholdTab> = _activeTab
    override val householdId: StateFlow<String?> = MutableStateFlow(householdId)
    override fun useCreate() {}
    override fun useJoin() {}
}

