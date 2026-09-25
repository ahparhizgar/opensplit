package com.opensplit.integration.expense

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
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalDecomposeApi::class)
interface AddExpenseFlowComponent : BackHandlerOwner {
  val panels:
      Value<
          ChildPanels<
              AddExpenseComponent.Config,
              AddExpenseComponent,
              DetailsConfig,
              DetailsChild,
              Nothing,
              Nothing,
          >
      >

  fun onBack()

  @Serializable
  sealed interface DetailsConfig {
    @Serializable data object PayerFlow : DetailsConfig

    @Serializable data object SplitFlow : DetailsConfig
  }

  sealed interface DetailsChild {
    data class PayerFlow(val component: PayerFlowComponent) : DetailsChild

    data class SplitFlow(val component: SplitFlowComponent) : DetailsChild
  }

  @Serializable
  data class Config(
      val groupId: String,
      val expenseId: String? = null,
  ) : TopLevelDestinationConfig
}

interface AddExpenseFlowComponentFactory {
  fun create(cContext: CContext, config: AddExpenseFlowComponent.Config): AddExpenseFlowComponent
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultAddExpenseFlowComponent(
    context: CContext,
    config: AddExpenseFlowComponent.Config,
    private val addExpenseFactory: AddExpenseComponentFactory,
    private val payerFlowFactory: PayerFlowComponentFactory,
    private val splitFlowFactory: SplitFlowComponentFactory,
) : AddExpenseFlowComponent, CContext by context, BackHandlerOwner {
  val scope = componentScope()
  private val groupId = config.groupId
  private val expenseId = config.expenseId

  private val nav =
      PanelsNavigation<AddExpenseComponent.Config, AddExpenseFlowComponent.DetailsConfig, Nothing>()

  private var _mainComponent: AddExpenseComponent? = null

  @OptIn(ExperimentalSerializationApi::class)
  override val panels:
      Value<
          ChildPanels<
              AddExpenseComponent.Config,
              AddExpenseComponent,
              AddExpenseFlowComponent.DetailsConfig,
              AddExpenseFlowComponent.DetailsChild,
              Nothing,
              Nothing,
          >
      > =
      childPanels(
          source = nav,
          serializers =
              AddExpenseComponent.Config.serializer() to
                  AddExpenseFlowComponent.DetailsConfig.serializer(),
          initialPanels = {
            Panels(
                main = AddExpenseComponent.Config(groupId, expenseId),
                mode = windowSizeHolder.windowSizeClass.value.toMode(),
            )
          },
          handleBackButton = true,
          mainFactory = { cfg, ctx ->
            addExpenseFactory
                .create(
                    context = ctx,
                    groupId = cfg.groupId,
                    expenseId = cfg.expenseId,
                    onNavigateToPayerFlow = {
                      nav.activateDetails(AddExpenseFlowComponent.DetailsConfig.PayerFlow)
                    },
                    onNavigateToSplitFlow = {
                      nav.activateDetails(AddExpenseFlowComponent.DetailsConfig.SplitFlow)
                    },
                    onFinished = { navigation.pop() },
                )
                .also { _mainComponent = it }
          },
          detailsFactory = { cfg, ctx ->
            val mainComponent = requireNotNull(_mainComponent) { "Main component not initialized" }
            when (cfg) {
              is AddExpenseFlowComponent.DetailsConfig.PayerFlow -> {
                AddExpenseFlowComponent.DetailsChild.PayerFlow(
                    payerFlowFactory.create(
                        context = ctx,
                        parentUiState = mainComponent.uiState,
                        onPayerSelected = { userId ->
                          val currentAmount =
                              (mainComponent.uiState.value.payAmounts
                                      as? PayAmountsUiState.OnePerson)
                                  ?.amount ?: ""
                          mainComponent.setPaidAmounts(
                              PayAmountsUiState.OnePerson(userId, currentAmount)
                          )
                          onDetailsBack()
                        },
                        onPaidAmountsDone = { amounts ->
                          mainComponent.setPaidAmounts(amounts)
                          onDetailsBack()
                        },
                        onFinished = ::onDetailsBack,
                    )
                )
              }
              is AddExpenseFlowComponent.DetailsConfig.SplitFlow -> {
                AddExpenseFlowComponent.DetailsChild.SplitFlow(
                    splitFlowFactory.create(
                        context = ctx,
                        groupId = groupId,
                        parentUiState = mainComponent.uiState,
                        onNavigateToPayerFlow = {
                          nav.activateDetails(AddExpenseFlowComponent.DetailsConfig.PayerFlow)
                        },
                        onQuickSplitDone = { amounts, method ->
                          mainComponent.setPaidAmounts(amounts)
                          mainComponent.setSplitMethod(method)
                          onDetailsBack()
                        },
                        onMoreSplitDone = { method ->
                          mainComponent.setSplitMethod(method)
                          onDetailsBack()
                        },
                        onFinished = ::onDetailsBack,
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
    return if (isExpanded) ChildPanelsMode.DUAL else ChildPanelsMode.SINGLE
  }

  private fun onDetailsBack() {
    nav.dismissDetails()
  }

  fun setMode(mode: ChildPanelsMode) {
    nav.setMode(mode)
  }

  override fun onBack() {
    nav.pop()
  }
}

class DefaultAddExpenseFlowComponentFactory(
    private val addExpenseFactory: AddExpenseComponentFactory,
    private val payerFlowFactory: PayerFlowComponentFactory,
    private val splitFlowFactory: SplitFlowComponentFactory,
) : AddExpenseFlowComponentFactory {
  override fun create(
      cContext: CContext,
      config: AddExpenseFlowComponent.Config,
  ): AddExpenseFlowComponent =
      DefaultAddExpenseFlowComponent(
          context = cContext,
          config = config,
          addExpenseFactory = addExpenseFactory,
          payerFlowFactory = payerFlowFactory,
          splitFlowFactory = splitFlowFactory,
      )
}

@OptIn(ExperimentalDecomposeApi::class)
class FakeAddExpenseFlowComponent(
    groupId: String = "h12345",
    addExpenseComponent: AddExpenseComponent = FakeAddExpenseComponent(),
) : AddExpenseFlowComponent {
  override val backHandler: BackHandler = BackDispatcher()

  override val panels:
      Value<
          ChildPanels<
              AddExpenseComponent.Config,
              AddExpenseComponent,
              AddExpenseFlowComponent.DetailsConfig,
              AddExpenseFlowComponent.DetailsChild,
              Nothing,
              Nothing,
          >
      > =
      MutableValue(
          ChildPanels(
              main = Child.Created(AddExpenseComponent.Config(groupId), addExpenseComponent),
              details = null,
              mode = ChildPanelsMode.SINGLE,
          )
      )

  fun setMode(mode: ChildPanelsMode) {}

  override fun onBack() {}
}
