package com.opensplit.features.household.my

import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.component.navigation
import com.opensplit.dto.household.HouseholdOverviewDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.features.household.HouseholdService
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface MyHouseholdsListComponent {
    val overview: StateFlow<HouseholdOverviewDto>
    val isLoading: StateFlow<Boolean>

    fun loadOverview(): Job
    fun leaveHousehold(householdId: String): Job
    fun onAddHouseholdClick()

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

    private val _overview = MutableStateFlow(HouseholdOverviewDto())
    override val overview: StateFlow<HouseholdOverviewDto> = _overview

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

    override fun onAddHouseholdClick() {
        navigation.pushNew(CreateJoinHouseholdComponent.Config())
    }

    class Factory(
        private val gateway: HouseholdService,
    ) : MyHouseholdsListComponent.Factory {
        override fun create(cContext: CContext): MyHouseholdsListComponent =
            DefaultMyHouseholdsListComponent(cContext, gateway)
    }
}

class FakeMyHouseholdsListComponent(
    override val overview: MutableStateFlow<HouseholdOverviewDto>
    = MutableStateFlow(
        HouseholdOverviewDto(
            households = listOf(
                HouseholdSummaryDto(
                    id = "1",
                    name = "Box Gym Bros",
                    memberCount = 2,
                    balance = 10.15,
                    currency = "IRR",
                    description = "Ali B. owes you IRR10.15"
                ),
                HouseholdSummaryDto(
                    id = "2",
                    name = "203.3",
                    memberCount = 3,
                    isSettled = true,
                ),
                HouseholdSummaryDto(
                    id = "3",
                    name = "گلابی",
                    memberCount = 4,
                    isSettled = true,
                )
            ),
            overallBalance = 10.15,
            overallCurrency = "IRR"
        )
    ),
    override val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false),
) : MyHouseholdsListComponent {
    override fun loadOverview() = Job()
    override fun leaveHousehold(householdId: String) = Job()
    override fun onAddHouseholdClick() {}
}
