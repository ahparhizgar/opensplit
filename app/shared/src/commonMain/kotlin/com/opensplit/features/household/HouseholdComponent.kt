package com.opensplit.features.household

import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.dto.household.HouseholdOverviewResponse
import com.opensplit.root.Destination
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable

enum class HouseholdTab { Create, Join }

interface HouseholdComponent : Destination {
    val activeTab: StateFlow<HouseholdTab>
    val createComponent: CreateHouseholdComponent
    val joinComponent: JoinHouseholdComponent
    val householdId: StateFlow<String?>
    val overview: StateFlow<HouseholdOverviewResponse>

    /** True while the initial overview is being fetched after login. */
    val isLoadingOverview: StateFlow<Boolean>
    fun useCreate()
    fun useJoin()
    suspend fun loadOverview()
    suspend fun switchHousehold(householdId: String)
    suspend fun leaveHousehold(householdId: String)

    @Serializable
    class Config : TopLevelDestinationConfig

    interface Factory {
        fun create(cContext: CContext, config: Config): HouseholdComponent
    }
}

class DefaultHouseholdComponent(
    context: CContext,
    private val gateway: HouseholdGateway,
) : HouseholdComponent, CContext by context {

    private val scope = componentScope()

    override val createComponent: CreateHouseholdComponent =
        DefaultCreateHouseholdComponent(context, gateway)
    override val joinComponent: JoinHouseholdComponent =
        DefaultJoinHouseholdComponent(context, gateway)

    private val _activeTab = MutableStateFlow(HouseholdTab.Create)
    override val activeTab: StateFlow<HouseholdTab> = _activeTab

    private val _overview = MutableStateFlow(HouseholdOverviewResponse())
    override val overview: StateFlow<HouseholdOverviewResponse> = _overview

    private val _isLoadingOverview = MutableStateFlow(true)
    override val isLoadingOverview: StateFlow<Boolean> = _isLoadingOverview

    override val householdId: StateFlow<String?> by lazy {
        combine(
            createComponent.uiState.map { it.householdId },
            joinComponent.uiState.map { it.householdId },
            overview.map { it.activeHouseholdId },
        ) { createId, joinId, activeId -> createId ?: joinId ?: activeId }
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = null,
            )
    }


    override fun useCreate() {
        _activeTab.value = HouseholdTab.Create
    }

    override fun useJoin() {
        _activeTab.value = HouseholdTab.Join
    }

    override suspend fun loadOverview() {
        try {
            val result = gateway.loadOverview()
            _overview.value = result
        } finally {
            // Clear the initial-load spinner regardless of success/failure so the
            // UI can show either the households page or the create/join setup screen.
            _isLoadingOverview.value = false
        }
    }

    override suspend fun switchHousehold(householdId: String) {
        _overview.value = gateway.switchHousehold(householdId)
    }

    override suspend fun leaveHousehold(householdId: String) {
        _overview.value = gateway.leaveHousehold(householdId)
    }

    class Factory(
        private val gateway: HouseholdGateway,
    ) : HouseholdComponent.Factory {
        override fun create(
            cContext: CContext,
            config: HouseholdComponent.Config
        ): HouseholdComponent = DefaultHouseholdComponent(
            context = cContext,
            gateway = gateway
        )
    }
}

@Suppress("unused")
class FakeHouseholdComponent(
    householdId: String? = "household-1",
) : HouseholdComponent {
    override val createComponent: CreateHouseholdComponent = FakeCreateHouseholdComponent()
    override val joinComponent: JoinHouseholdComponent = FakeJoinHouseholdComponent()
    private val _activeTab = MutableStateFlow(HouseholdTab.Create)
    override val activeTab: StateFlow<HouseholdTab> = _activeTab
    override val householdId: StateFlow<String?> = MutableStateFlow(householdId)
    override val overview: StateFlow<HouseholdOverviewResponse> =
        MutableStateFlow(HouseholdOverviewResponse(activeHouseholdId = householdId))
    override val isLoadingOverview: StateFlow<Boolean> = MutableStateFlow(false)
    override fun useCreate() {}
    override fun useJoin() {}
    override suspend fun loadOverview() {}
    override suspend fun switchHousehold(householdId: String) {}
    override suspend fun leaveHousehold(householdId: String) {}
}

