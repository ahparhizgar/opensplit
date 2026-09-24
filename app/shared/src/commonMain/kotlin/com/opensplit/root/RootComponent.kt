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
import com.opensplit.component.WindowSizeHolder
import com.opensplit.component.componentScope
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.AuthComponentFactory
import com.opensplit.features.auth.TokenStorage
import com.opensplit.features.expense.AddExpenseFlowComponent
import com.opensplit.features.expense.AddExpenseFlowComponentFactory
import com.opensplit.features.expense.ExpenseDetailsComponent
import com.opensplit.features.expense.ExpenseDetailsComponentFactory
import com.opensplit.features.group.createjoin.CreateGroupComponent
import com.opensplit.features.group.createjoin.CreateGroupComponentFactory
import com.opensplit.features.group.createjoin.GroupSelectionComponent
import com.opensplit.features.group.createjoin.GroupSelectionComponentFactory
import com.opensplit.features.group.createjoin.JoinGroupComponent
import com.opensplit.features.group.createjoin.JoinGroupComponentFactory
import com.opensplit.features.group.details.GroupDetailsComponent
import com.opensplit.features.group.details.GroupFlowComponent
import com.opensplit.features.group.details.GroupFlowComponentFactory
import com.opensplit.features.group.my.MyGroupsListComponent
import com.opensplit.features.group.my.MyGroupsListComponentFactory
import com.opensplit.features.group.settings.GroupSettingsComponent
import com.opensplit.features.group.settings.GroupSettingsComponentFactory
import com.opensplit.features.profile.ProfileComponent
import com.opensplit.features.profile.ProfileComponentFactory
import com.opensplit.repository.GroupRepository
import com.opensplit.splash.SplashDestination
import com.opensplit.sync.SyncDaemon
import com.opensplit.usermessage.MessageHolder
import kotlin.reflect.KClass
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope

interface RootComponent {
  val backHandler: BackHandler
  val childStack: Value<ChildStack<*, Any>>
  val messageHolder: MessageHolder
  val windowSizeHolder: WindowSizeHolder

  fun onBack()
}

interface RootComponentFactory {
  fun create(context: CContext): RootComponent
}

class DefaultRootComponent(
    cContext: CContext,
    private val componentProvider: ComponentProvider,
    private val tokenStorage: TokenStorage,
    syncDaemon: SyncDaemon,
    groupRepository: GroupRepository,
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
    syncDaemon.start()
    groupRepository.refresh()

    scope.launch {
      try {
        val token = tokenStorage.getAccessToken()
        if (!token.isNullOrEmpty()) {
          rootNavigation.replaceAll(MyGroupsListComponent.Config)
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
          componentProvider.provide(AuthComponentFactory::class).create(cContext)

      is GroupSelectionComponent.Config ->
          componentProvider.provide(GroupSelectionComponentFactory::class).create(cContext)

      is CreateGroupComponent.Config ->
          componentProvider.provide(CreateGroupComponentFactory::class).create(cContext)

      is JoinGroupComponent.Config ->
          componentProvider.provide(JoinGroupComponentFactory::class).create(cContext)

      is MyGroupsListComponent.Config ->
          componentProvider.provide(MyGroupsListComponentFactory::class).create(cContext)

      is GroupFlowComponent.Config ->
          componentProvider.provide(GroupFlowComponentFactory::class).create(cContext, config)

      is GroupDetailsComponent.Config ->
          componentProvider
              .provide(GroupFlowComponentFactory::class)
              .create(cContext, GroupFlowComponent.Config(config.groupId))

      is GroupSettingsComponent.Config ->
          componentProvider.provide(GroupSettingsComponentFactory::class).create(cContext, config)

      is AddExpenseFlowComponent.Config ->
          componentProvider
              .provide(AddExpenseFlowComponentFactory::class)
              .create(
                  cContext = cContext,
                  config = config,
              )

      is ExpenseDetailsComponent.Config ->
          componentProvider
              .provide(ExpenseDetailsComponentFactory::class)
              .create(
                  context = cContext,
                  config = config,
                  onBack = { rootNavigation.pop() },
              )

      is ProfileComponent.Config ->
          componentProvider.provide(ProfileComponentFactory::class).create(cContext)

      else -> error("Destination not defined in createChild")
    }
  }
}

class DefaultRootComponentFactory(
    private val componentProvider: ComponentProvider,
    private val tokenStorage: TokenStorage,
    private val groupRepository: GroupRepository,
    private val syncDaemon: SyncDaemon,
) : RootComponentFactory {
  override fun create(context: CContext): RootComponent =
      DefaultRootComponent(
          cContext = context,
          componentProvider = componentProvider,
          tokenStorage = tokenStorage,
          groupRepository = groupRepository,
          syncDaemon = syncDaemon,
      )
}

class FakeRootComponent : RootComponent {
  override val backHandler: BackHandler = BackDispatcher()
  override val messageHolder: MessageHolder = MessageHolder()
  override val windowSizeHolder: WindowSizeHolder = WindowSizeHolder()

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
