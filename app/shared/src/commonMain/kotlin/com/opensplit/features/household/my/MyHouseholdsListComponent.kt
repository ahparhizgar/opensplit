package com.opensplit.features.household.my

import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.dto.household.FakeHouseholdDtoFactory
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.repository.HouseholdRepository
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface MyHouseholdsListComponent {
  val uiState: Value<MyHouseholdsUiState>
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

data class MyHouseholdsUiState(
    val households: List<HouseholdDto> = emptyList(),
    val overallBalance: Double = 0.0,
    val overallCurrency: String = "IRR",
)

class DefaultMyHouseholdsListComponent(
    context: CContext,
    private val repository: HouseholdRepository,
) : MyHouseholdsListComponent, CContext by context {

  private val scope = componentScope()

  init {
    scope.launch { repository.getHouseholds().collect { updateState(it) } }
    doOnCreate { loadOverview() }
  }

  private val _uiState = MutableValue(MyHouseholdsUiState())
  override val uiState: Value<MyHouseholdsUiState> = _uiState

  private val _isLoading = MutableValue(true)
  override val isLoading: Value<Boolean> = _isLoading

  private val _isSettledExpanded = MutableValue(false)
  override val isSettledExpanded: Value<Boolean> = _isSettledExpanded

  override fun loadOverview() = scope.launch {
    try {
      repository.refreshHouseholds()
    } finally {
      _isLoading.value = false
    }
  }

  private fun updateState(households: List<HouseholdDto>) {
    _uiState.update {
      it.copy(
          households = households,
          overallBalance = households.sumOf { h -> h.balance },
          overallCurrency = "IRR",
      )
    }
  }

  override fun onHouseholdClick(id: String) {
    navigation.pushNew(HouseholdDetailsComponent.Config(id))
  }

  override fun leaveHousehold(householdId: String) = scope.launch {
    repository.leaveHousehold(householdId)
  }

  override fun onAddHouseholdClick() {
    navigation.pushNew(CreateJoinHouseholdComponent.Config())
  }

  override fun onToggleSettledExpanded() {
    _isSettledExpanded.value = !_isSettledExpanded.value
  }

  class Factory(
      private val repository: HouseholdRepository,
  ) : MyHouseholdsListComponent.Factory {
    override fun create(cContext: CContext): MyHouseholdsListComponent =
        DefaultMyHouseholdsListComponent(cContext, repository)
  }
}

class FakeMyHouseholdsListComponent(
    uiState: MyHouseholdsUiState =
        MyHouseholdsUiState(
            households =
                listOf(
                    FakeHouseholdDtoFactory.create(
                            id = "1",
                            name = "Amirs House",
                        )
                        .copy(balance = 10.15),
                    FakeHouseholdDtoFactory.create(
                        id = "2",
                        name = "203.3",
                    ),
                    FakeHouseholdDtoFactory.create(
                        id = "3",
                        name = "Shomal Trip",
                    ),
                ),
            overallBalance = 10.15,
            overallCurrency = "IRR",
        ),
    override val isLoading: MutableValue<Boolean> = MutableValue(false),
    override val isSettledExpanded: MutableValue<Boolean> = MutableValue(false),
) : MyHouseholdsListComponent {
  override val uiState: Value<MyHouseholdsUiState> = MutableValue(uiState)

  override fun loadOverview() = Job()

  override fun leaveHousehold(householdId: String) = Job()

  override fun onAddHouseholdClick() {}

  override fun onToggleSettledExpanded() {
    isSettledExpanded.value = !isSettledExpanded.value
  }
}
