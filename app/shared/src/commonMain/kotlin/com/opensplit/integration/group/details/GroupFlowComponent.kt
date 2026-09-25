package com.opensplit.integration.group.details

import androidx.window.core.layout.WindowSizeClass
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.dismissDetails
import com.arkivanov.decompose.router.panels.pop
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.integration.expense.ExpenseDetailsComponent
import com.opensplit.integration.expense.ExpenseDetailsComponentFactory
import com.opensplit.integration.group.settings.GroupSettingsComponent
import com.opensplit.integration.group.settings.GroupSettingsComponentFactory
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalDecomposeApi::class)
interface GroupFlowComponent : BackHandlerOwner {
  val groupId: String
  val panels:
      Value<
          ChildPanels<
              GroupDetailsComponent.Config,
              GroupDetailsComponent,
              DetailsConfig,
              DetailsChild,
              Nothing,
              Nothing,
          >
      >

  fun setMode(mode: ChildPanelsMode)

  fun onBack()

  @Serializable
  sealed interface DetailsConfig {
    @Serializable
    data class Settings(val groupId: String, val isUserRequested: Boolean = false) : DetailsConfig

    @Serializable data class Expense(val groupId: String, val expenseId: String) : DetailsConfig
  }

  sealed interface DetailsChild {
    data class Settings(val component: GroupSettingsComponent) : DetailsChild

    data class Expense(val component: ExpenseDetailsComponent) : DetailsChild
  }

  @Serializable data class Config(val groupId: String) : TopLevelDestinationConfig
}

interface GroupFlowComponentFactory {
  fun create(cContext: CContext, config: GroupFlowComponent.Config): GroupFlowComponent
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultGroupFlowComponent(
    context: CContext,
    config: GroupFlowComponent.Config,
    private val groupDetailsFactory: GroupDetailsComponentFactory,
    private val groupSettingsFactory: GroupSettingsComponentFactory,
    private val expenseDetailsFactory: ExpenseDetailsComponentFactory,
) : GroupFlowComponent, CContext by context, BackHandlerOwner {
  val scope = componentScope()
  override val groupId: String = config.groupId

  private val nav =
      PanelsNavigation<GroupDetailsComponent.Config, GroupFlowComponent.DetailsConfig, Nothing>()

  @OptIn(ExperimentalSerializationApi::class)
  override val panels:
      Value<
          ChildPanels<
              GroupDetailsComponent.Config,
              GroupDetailsComponent,
              GroupFlowComponent.DetailsConfig,
              GroupFlowComponent.DetailsChild,
              Nothing,
              Nothing,
          >
      > =
      childPanels(
          source = nav,
          serializers =
              GroupDetailsComponent.Config.serializer() to
                  GroupFlowComponent.DetailsConfig.serializer(),
          initialPanels = {
            Panels(
                main = GroupDetailsComponent.Config(groupId),
                mode = windowSizeHolder.windowSizeClass.value.toMode(),
            )
          },
          handleBackButton = true,
          mainFactory = { cfg, ctx ->
            groupDetailsFactory.create(
                cContext = ctx,
                config = cfg,
                onExpenseClicked = { expense ->
                  nav.activateDetails(GroupFlowComponent.DetailsConfig.Expense(groupId, expense.id))
                },
                onSettingsClick = {
                  nav.activateDetails(
                      GroupFlowComponent.DetailsConfig.Settings(groupId, isUserRequested = true)
                  )
                },
                onBack = { navigation.pop() },
            )
          },
          detailsFactory = { cfg, ctx ->
            when (cfg) {
              is GroupFlowComponent.DetailsConfig.Settings -> {
                GroupFlowComponent.DetailsChild.Settings(
                    groupSettingsFactory.create(
                        cContext = ctx,
                        config = GroupSettingsComponent.Config(cfg.groupId),
                        onBack = ::onDetailsBack,
                    )
                )
              }

              is GroupFlowComponent.DetailsConfig.Expense -> {
                GroupFlowComponent.DetailsChild.Expense(
                    expenseDetailsFactory.create(
                        context = ctx,
                        config = ExpenseDetailsComponent.Config(cfg.groupId, cfg.expenseId),
                        onBack = ::onDetailsBack,
                    )
                )
              }
            }
          },
      )

  init {
    scope.launch {
      windowSizeHolder.windowSizeClass.collect { windowSizeClass ->
        val mode = windowSizeClass.toMode()
        setMode(mode)
      }
    }
  }

  private fun WindowSizeClass.toMode(): ChildPanelsMode {
    val isExpanded = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val mode = if (isExpanded) ChildPanelsMode.DUAL else ChildPanelsMode.SINGLE
    return mode
  }

  private fun onDetailsBack() {
    nav.dismissDetails()
  }

  override fun setMode(mode: ChildPanelsMode) {
    nav.setMode(mode)
  }

  override fun onBack() {
    nav.pop()
  }
}

class DefaultGroupFlowComponentFactory(
    private val groupDetailsFactory: GroupDetailsComponentFactory,
    private val groupSettingsFactory: GroupSettingsComponentFactory,
    private val expenseDetailsFactory: ExpenseDetailsComponentFactory,
) : GroupFlowComponentFactory {
  override fun create(
      cContext: CContext,
      config: GroupFlowComponent.Config,
  ): GroupFlowComponent =
      DefaultGroupFlowComponent(
          context = cContext,
          config = config,
          groupDetailsFactory = groupDetailsFactory,
          groupSettingsFactory = groupSettingsFactory,
          expenseDetailsFactory = expenseDetailsFactory,
      )
}

@OptIn(ExperimentalDecomposeApi::class)
class FakeGroupFlowComponent(
    override val groupId: String = "h12345",
    groupDetailsComponent: GroupDetailsComponent = FakeGroupDetailsComponent(groupId),
) : GroupFlowComponent {
  override val backHandler: BackHandler = BackDispatcher()

  override val panels:
      Value<
          ChildPanels<
              GroupDetailsComponent.Config,
              GroupDetailsComponent,
              GroupFlowComponent.DetailsConfig,
              GroupFlowComponent.DetailsChild,
              Nothing,
              Nothing,
          >
      > =
      MutableValue(
          ChildPanels(
              main = Child.Created(GroupDetailsComponent.Config(groupId), groupDetailsComponent),
              details = null,
              mode = ChildPanelsMode.SINGLE,
          )
      )

  override fun setMode(mode: ChildPanelsMode) {}

  override fun onBack() {}
}
