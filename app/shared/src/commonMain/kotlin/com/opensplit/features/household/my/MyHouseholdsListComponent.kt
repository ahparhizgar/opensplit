package com.opensplit.features.household.my

import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.dto.household.HouseholdOverviewDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.features.household.HouseholdApi
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface MyHouseholdsListComponent {
  val overview: Value<HouseholdOverviewDto>
  val isLoading: Value<Boolean>
  val isSettledExpanded: Value<Boolean>

  fun loadOverview(): Job

  fun leaveHousehold(householdId: String): Job

  fun onAddHouseholdClick()

  fun onHouseholdClick(id: String) {}

  fun onToggleSettledExpanded()

  @Serializable data object Config : TopLevelDestinationConfig

  interface Factory {
    fun create(cContext: CContext): MyHouseholdsListComponent
  }
}

class DefaultMyHouseholdsListComponent(
    context: CContext,
    private val gateway: HouseholdApi,
) : MyHouseholdsListComponent, CContext by context {

  private val scope = componentScope()

  init {
    doOnCreate { loadOverview() }
  }

  private val _overview = MutableValue(HouseholdOverviewDto())
  override val overview: Value<HouseholdOverviewDto> = _overview

  private val _isLoading = MutableValue(true)
  override val isLoading: Value<Boolean> = _isLoading

  private val _isSettledExpanded = MutableValue(false)
  override val isSettledExpanded: Value<Boolean> = _isSettledExpanded

  override fun loadOverview() = scope.launch {
    try {
      val result = gateway.loadOverview()
      _overview.value = result
    } finally {
      _isLoading.value = false
    }
  }

  override fun onHouseholdClick(id: String) {
    navigation.pushNew(HouseholdDetailsComponent.Config(id))
  }

  override fun leaveHousehold(householdId: String) = scope.launch {
    _overview.value = gateway.leaveHousehold(householdId)
  }

  override fun onAddHouseholdClick() {
    navigation.pushNew(CreateJoinHouseholdComponent.Config())
  }

  override fun onToggleSettledExpanded() {
    _isSettledExpanded.value = !_isSettledExpanded.value
  }

  class Factory(
      private val gateway: HouseholdApi,
  ) : MyHouseholdsListComponent.Factory {
    override fun create(cContext: CContext): MyHouseholdsListComponent =
        DefaultMyHouseholdsListComponent(cContext, gateway)
  }
}

class FakeMyHouseholdsListComponent(
    override val overview: MutableValue<HouseholdOverviewDto> =
        MutableValue(
            HouseholdOverviewDto(
                households =
                    listOf(
                        HouseholdSummaryDto(
                            id = "1",
                            name = "Box Gym Bros",
                            memberCount = 2,
                            balance = 10.15,
                            currency = "IRR",
                            description = "Ali B. owes you IRR10.15",
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
                        ),
                    ),
                overallBalance = 10.15,
                overallCurrency = "IRR",
            )
        ),
    override val isLoading: MutableValue<Boolean> = MutableValue(false),
    override val isSettledExpanded: MutableValue<Boolean> = MutableValue(false),
) : MyHouseholdsListComponent {
  override fun loadOverview() = Job()

  override fun leaveHousehold(householdId: String) = Job()

  override fun onAddHouseholdClick() {}

  override fun onToggleSettledExpanded() {
    isSettledExpanded.value = !isSettledExpanded.value
  }
}
