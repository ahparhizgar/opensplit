package com.opensplit.features.household

import com.opensplit.component.CContext
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface HouseholdDetailComponent {
    val householdId: String
    val householdName: StateFlow<String>

    @Serializable
    data class Config(val householdId: String) : TopLevelDestinationConfig

    interface Factory {
        fun create(cContext: CContext, config: Config): HouseholdDetailComponent
    }
}

class DefaultHouseholdDetailComponent(
    context: CContext,
    config: HouseholdDetailComponent.Config,
) : HouseholdDetailComponent, CContext by context {

    override val householdId: String = config.householdId
    // For now, it's just a placeholder or we could fetch it.
    // Given "minimal", I'll just use a flow with a placeholder.
    override val householdName: StateFlow<String> = MutableStateFlow("Household $householdId")

    class Factory : HouseholdDetailComponent.Factory {
        override fun create(
            cContext: CContext,
            config: HouseholdDetailComponent.Config
        ): HouseholdDetailComponent = DefaultHouseholdDetailComponent(cContext, config)
    }
}

class FakeHouseholdDetailComponent(
    override val householdId: String = "h1",
    name: String = "Fake Household"
) : HouseholdDetailComponent {
    override val householdName: StateFlow<String> = MutableStateFlow(name)
}
