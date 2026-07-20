package com.opensplit.root

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.features.household.my.MyHouseholdsListComponent
import com.opensplit.features.household.settings.HouseholdSettingsComponent
import com.opensplit.splash.SplashDestination
import com.opensplit.usermessage.MessageHolder
import kotlin.reflect.KClass
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope

interface RootComponent {
  val backHandler: BackHandler
  val childStack: Value<ChildStack<*, Any>>
  val messageHolder: MessageHolder

  fun onBack()

  interface Factory {
    fun create(context: CContext): RootComponent
  }
}

class DefaultRootComponent(
    cContext: CContext,
    private val componentProvider: ComponentProvider,
    private val tokenStorage: TokenStorage,
) : RootComponent, CContext by cContext {
  val scope = componentScope()

  private val rootNavigation = StackNavigation<TopLevelDestinationConfig>()
  override val backHandler: BackHandler = cContext.backHandler
  override val messageHolder: MessageHolder = MessageHolder()

  override fun onBack() {
    rootNavigation.pop()
  }

  init {
    @Suppress("UNCHECKED_CAST")
    cContext.navigation = rootNavigation as StackNavigation<Any>
    messageShower = messageHolder

    scope.launch {
      try {
        val token = tokenStorage.getAccessToken()
        if (!token.isNullOrEmpty()) {
          rootNavigation.replaceAll(MyHouseholdsListComponent.Config)
        } else {
          rootNavigation.replaceAll(AuthComponent.Config)
        }
      } catch (_: Throwable) {
        // Swallow any persistence errors; default to auth flow.
      }
    }
  }

  override val childStack: Value<ChildStack<*, Any>> =
      cContext.childStack(
          source = rootNavigation,
          serializer = null,
          initialConfiguration = SplashDestination,
          handleBackButton = true,
          childFactory = ::createChild,
      )

  private fun createChild(config: TopLevelDestinationConfig, cContext: CContext): Any {
    return when (config) {
      is SplashDestination -> SplashDestination
      is AuthComponent.Config ->
          componentProvider.provide(AuthComponent.Factory::class).create(cContext)

      is CreateJoinHouseholdComponent.Config ->
          componentProvider.provide(CreateJoinHouseholdComponent.Factory::class).create(cContext)

      is MyHouseholdsListComponent.Config ->
          componentProvider.provide(MyHouseholdsListComponent.Factory::class).create(cContext)

      is HouseholdDetailsComponent.Config ->
          componentProvider
              .provide(HouseholdDetailsComponent.Factory::class)
              .create(cContext, config)

      is HouseholdSettingsComponent.Config ->
          componentProvider
              .provide(HouseholdSettingsComponent.Factory::class)
              .create(cContext, config)

      is AddExpenseComponent.Config ->
          componentProvider
              .provide(AddExpenseComponent.Factory::class)
              .create(cContext, config, onFinished = { rootNavigation.pop() })

      else -> error("Destination not defined in createChild")
    }
  }

  class Factory(
      private val componentProvider: ComponentProvider,
      private val tokenStorage: TokenStorage,
  ) : RootComponent.Factory {
    override fun create(context: CContext): RootComponent =
        DefaultRootComponent(context, componentProvider, tokenStorage)
  }
}

class FakeRootComponent : RootComponent {
  override val backHandler: BackHandler = BackDispatcher()
  override val messageHolder: MessageHolder = MessageHolder()

  override fun onBack() {}

  override val childStack: Value<ChildStack<*, Any>> =
      MutableValue(
          ChildStack(
              active = Child.Created(SplashDestination, SplashDestination),
              backStack = emptyList(),
          )
      )
}

interface Destination

interface TopLevelDestinationConfig

interface ComponentProvider {
  fun <T : Any> provide(kClass: KClass<T>): T
}

class KoinComponentProvider(private val scope: Scope) : ComponentProvider {
  override fun <T : Any> provide(kClass: KClass<T>): T {
    return scope.get(clazz = kClass)
  }
}
