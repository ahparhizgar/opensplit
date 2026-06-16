package com.opensplit.features.household.my

import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.dto.household.HouseholdOverviewResponse
import com.opensplit.features.household.HouseholdService
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface MyHouseholdsListComponent {
    val overview: StateFlow<HouseholdOverviewResponse>
    val isLoading: StateFlow<Boolean>

    fun loadOverview(): Job
    fun leaveHousehold(householdId: String): Job

    @Serializable
    class Config : TopLevelDestinationConfig

    interface Factory {
        fun create(cContext: CContext): MyHouseholdsListComponent
    }
}

class DefaultMyHouseholdsListComponent(
    context: CContext,
    private val gateway: HouseholdService,
) : MyHouseholdsListComponent, CContext by context {

    private val scope = componentScope()

    init {
        doOnCreate {
            loadOverview()
        }
    }

    private val _overview = MutableStateFlow(HouseholdOverviewResponse())
    override val overview: StateFlow<HouseholdOverviewResponse> = _overview

    private val _isLoading = MutableStateFlow(true)
    override val isLoading: StateFlow<Boolean> = _isLoading

    override fun loadOverview() = scope.launch {
        try {
            val result = gateway.loadOverview()
            _overview.value = result
        } finally {
            _isLoading.value = false
        }
    }

    override fun leaveHousehold(householdId: String) = scope.launch {
        _overview.value = gateway.leaveHousehold(householdId)
    }

    class Factory(
        private val gateway: HouseholdService,
    ) : MyHouseholdsListComponent.Factory {
        override fun create(cContext: CContext): MyHouseholdsListComponent =
            DefaultMyHouseholdsListComponent(cContext, gateway)
    }
}

class FakeMyHouseholdsListComponent(
    override val overview: MutableStateFlow<HouseholdOverviewResponse>
    = MutableStateFlow(HouseholdOverviewResponse()),
    override val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false),
) : MyHouseholdsListComponent {
    override fun loadOverview() = Job()
    override fun leaveHousehold(householdId: String) = Job()
}
