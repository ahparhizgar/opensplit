package com.opensplit.root

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.component.CallBackNavigationOwner
import com.opensplit.component.componentScope
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthMode
import com.opensplit.features.auth.FakeAuthComponent
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.features.household.my.MyHouseholdsListComponent
import com.opensplit.features.household.settings.HouseholdSettingsComponent
import com.opensplit.splash.SplashDestination
import kotlin.reflect.KClass
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.scope.Scope

interface RootComponent {
  val childStack: Value<ChildStack<*, Any>>

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

  private val navigation = StackNavigation<TopLevelDestinationConfig>()

  init {
    val navOwner = cContext.stackNavigationOwner as CallBackNavigationOwner
    @Suppress("UNCHECKED_CAST")
    navOwner.navigation = navigation as StackNavigation<Any>

    scope.launch {
      try {
        val token = tokenStorage.getAccessToken()
        if (!token.isNullOrEmpty()) {
          navigation.navigate { listOf(MyHouseholdsListComponent.Config()) }
        } else {
          navigation.navigate { listOf(AuthComponent.Config(AuthMode.SignUp)) }
        }
      } catch (_: Throwable) {
        // Swallow any persistence errors; default to auth flow.
      }
    }
  }

  override val childStack: Value<ChildStack<*, Any>> =
      childStack(
          source = navigation,
          serializer = null,
          initialConfiguration = SplashDestination,
          handleBackButton = true,
          childFactory = ::createChild,
      )

  private fun createChild(config: TopLevelDestinationConfig, cContext: CContext): Any {
    return when (config) {
      is SplashDestination -> SplashDestination
      is AuthComponent.Config ->
          componentProvider.provide(AuthComponent.Factory::class).create(cContext, config)

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
  override val childStack: Value<ChildStack<*, Any>> =
      MutableValue(
          ChildStack(
              active = Child.Created(AuthComponent.Config(AuthMode.SignUp), FakeAuthComponent()),
              backStack = emptyList(),
          )
      )
}

interface Destination

@Serializable interface TopLevelDestinationConfig

interface ComponentProvider {
  fun <T : Any> provide(kClass: KClass<T>): T
}

class KoinComponentProvider(private val scope: Scope) : ComponentProvider {
  override fun <T : Any> provide(kClass: KClass<T>): T {
    return scope.get(clazz = kClass)
  }
}
