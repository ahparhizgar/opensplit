package com.opensplit.features.household.details

import com.opensplit.component.CContext
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface HouseholdDetailsComponent {
    val householdId: String
    val householdName: StateFlow<String>

    @Serializable
    data class Config(val householdId: String) : TopLevelDestinationConfig

    interface Factory {
        fun create(cContext: CContext, config: Config): HouseholdDetailsComponent
    }
}

class DefaultHouseholdDetailsComponent(
    context: CContext,
    config: HouseholdDetailsComponent.Config,
) : HouseholdDetailsComponent, CContext by context {

    override val householdId: String = config.householdId
    // For now, it's just a placeholder or we could fetch it.
    // Given "minimal", I'll just use a flow with a placeholder.
    override val householdName: StateFlow<String> = MutableStateFlow("Household $householdId")

    class Factory : HouseholdDetailsComponent.Factory {
        override fun create(
            cContext: CContext,
            config: HouseholdDetailsComponent.Config
        ): HouseholdDetailsComponent = DefaultHouseholdDetailsComponent(cContext, config)
    }
}

class FakeHouseholdDetailsComponent(
    override val householdId: String = "h1",
    name: String = "Fake Household"
) : HouseholdDetailsComponent {
    override val householdName: StateFlow<String> = MutableStateFlow(name)
}
