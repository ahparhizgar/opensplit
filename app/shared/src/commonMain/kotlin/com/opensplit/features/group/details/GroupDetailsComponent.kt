package com.opensplit.features.group.details

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.domain.Expense
import com.opensplit.domain.Group
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.group.settings.GroupSettingsComponent
import com.opensplit.repository.ExpenseRepository
import com.opensplit.repository.GroupRepository
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface GroupDetailsComponent {
  val groupId: String
  val uiState: StateFlow<UiState>

  fun onAddMemberClicked() {}

  fun onAddExpenseClicked() {}

  fun onExpenseClicked(expense: Expense) {}

  fun onBack() {}

  fun onSettingsClick() {}

  @Serializable data class Config(val groupId: String) : TopLevelDestinationConfig

  data class UiState(
      val group: Group? = null,
      val expenses: List<Expense> = emptyList(),
      val error: String? = null,
  )
}

interface GroupDetailsComponentFactory {
  fun create(
      cContext: CContext,
      config: GroupDetailsComponent.Config,
      onExpenseClicked: ((Expense) -> Unit)? = null,
      onSettingsClick: (() -> Unit)? = null,
      onBack: (() -> Unit)? = null,
  ): GroupDetailsComponent
}

class DefaultGroupDetailsComponent(
    context: CContext,
    config: GroupDetailsComponent.Config,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val onExpenseClickedCallback: ((Expense) -> Unit)? = null,
    private val onSettingsClickCallback: (() -> Unit)? = null,
    private val onBackCallback: (() -> Unit)? = null,
) : GroupDetailsComponent, CContext by context {

  override val groupId: String = config.groupId
  private val _uiState = MutableStateFlow(GroupDetailsComponent.UiState())
  override val uiState: StateFlow<GroupDetailsComponent.UiState> = _uiState

  init {
    componentScope().launch {
      groupRepository.observeGroup(groupId).collect { group ->
        _uiState.update { it.copy(group = group) }
      }
    }
    componentScope().launch {
      expenseRepository.getExpenses(groupId).collect { expenses ->
        _uiState.update { it.copy(expenses = expenses) }
      }
    }
  }

  override fun onAddMemberClicked() {
    // TODO: Implement add member
  }

  override fun onAddExpenseClicked() {
    navigation.pushNew(AddExpenseComponent.Config(groupId = groupId))
  }

  override fun onExpenseClicked(expense: Expense) {
    if (onExpenseClickedCallback != null) {
      onExpenseClickedCallback.invoke(expense)
    } else {
      navigation.pushNew(ExpenseDetailsComponent.Config(groupId, expense.id))
    }
  }

  override fun onSettingsClick() {
    if (onSettingsClickCallback != null) {
      onSettingsClickCallback.invoke()
    } else {
      navigation.pushNew(GroupSettingsComponent.Config(groupId))
    }
  }

  override fun onBack() {
    if (onBackCallback != null) {
      onBackCallback.invoke()
    } else {
      navigation.pop()
    }
  }
}

class DefaultGroupDetailsComponentFactory(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
) : GroupDetailsComponentFactory {
  override fun create(
      cContext: CContext,
      config: GroupDetailsComponent.Config,
      onExpenseClicked: ((Expense) -> Unit)?,
      onSettingsClick: (() -> Unit)?,
      onBack: (() -> Unit)?,
  ): GroupDetailsComponent =
      DefaultGroupDetailsComponent(
          context = cContext,
          config = config,
          groupRepository = groupRepository,
          expenseRepository = expenseRepository,
          onExpenseClickedCallback = onExpenseClicked,
          onSettingsClickCallback = onSettingsClick,
          onBackCallback = onBack,
      )
}

class FakeGroupDetailsComponent(
    override val groupId: String = "h12345",
    uiState: GroupDetailsComponent.UiState = GroupDetailsComponent.UiState(),
) : GroupDetailsComponent {
  override val uiState: StateFlow<GroupDetailsComponent.UiState> = MutableStateFlow(uiState)
}
