package com.opensplit.component

import com.ahparhizgar.katch.ApiCallError
import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.opensplit.remote.userMessage
import com.opensplit.usermessage.MessageHolder
import com.opensplit.usermessage.MessageShower
import com.opensplit.usermessage.SnackbarMessage
import com.opensplit.usermessage.UserMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

interface CContext : GenericComponentContext<CContext> {
  var navigation: StackNavigation<Any>
  var messageShower: MessageShower
}

class DefaultCContext(
    override val lifecycle: Lifecycle,
    override val stateKeeper: StateKeeper = StateKeeperDispatcher(),
    override val instanceKeeper: InstanceKeeper =
        InstanceKeeperDispatcher().also { lifecycle.doOnDestroy(it::destroy) },
    override val backHandler: BackHandler = BackDispatcher(),
    override var navigation: StackNavigation<Any> = FakeStackNavigation(),
    override var messageShower: MessageShower = MessageHolder(),
) : CContext {
  override val componentContextFactory: ComponentContextFactory<CContext> =
      ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
        DefaultCContext(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            instanceKeeper = instanceKeeper,
            backHandler = backHandler,
            navigation = navigation,
            messageShower = messageShower,
        )
      }
}

fun CContext.componentScope(): CoroutineScope {
  return CoroutineScope(
          Dispatchers.Main +
              SupervisorJob() +
              snackbarExceptionHandler(
                  messageShower,
                  CoroutineScope(Dispatchers.Main + SupervisorJob()).also {
                    lifecycle.doOnDestroy { it.cancel("Component was destroyed.") }
                  },
              )
      )
      .also { lifecycle.doOnDestroy { it.cancel("Component was destroyed.") } }
}

fun snackbarExceptionHandler(messageShower: MessageShower, scope: CoroutineScope) =
    CoroutineExceptionHandler { _, exception ->
      if (exception is ApiCallError) {
        scope.launch {
          messageShower.showSnackbar(
              SnackbarMessage(
                  content = exception.userMessage,
                  tone = UserMessage.Tone.Error,
              )
          )
        }
      } else {
        throw exception
      }
    }

fun defaultCContext(componentContext: ComponentContext) =
    DefaultCContext(
        lifecycle = componentContext.lifecycle,
        stateKeeper = componentContext.stateKeeper,
        instanceKeeper = componentContext.instanceKeeper,
        backHandler = componentContext.backHandler,
    )

class FakeStackNavigation<C : Any> : StackNavigation<C> {
  private val _stack = mutableListOf<C>()
  val stack: List<C>
    get() = _stack

  override fun navigate(
      transformer: (stack: List<C>) -> List<C>,
      onComplete: (newStack: List<C>, oldStack: List<C>) -> Unit,
  ) {
    val oldStack = _stack.toList()
    _stack.clear()
    val newStack = transformer(oldStack)
    _stack.addAll(newStack)
    onComplete(_stack, oldStack)
  }

  override fun subscribe(observer: (StackNavigation.Event<C>) -> Unit): Cancellation {
    // For testing purposes, we can ignore subscriptions
    return Cancellation {}
  }
}

fun CContext.fakeStack() =
    (navigation as? FakeStackNavigation<Any>)?.stack
        ?: error("navigation by default has a FakeStackNavigation unless is set by specific test!")

class TestCContext : CContext {
  val lifecycleRegistry = LifecycleRegistry()
  val fakeStackNavigation = FakeStackNavigation<Any>()
  val backDispatcher = BackDispatcher()
  val messageHolder = MessageHolder()

  override val lifecycle: Lifecycle = lifecycleRegistry

  override val stateKeeper: StateKeeper = StateKeeperDispatcher()

  override val instanceKeeper: InstanceKeeper =
      InstanceKeeperDispatcher().also { lifecycle.doOnDestroy(it::destroy) }

  override val backHandler: BackHandler = backDispatcher

  override val componentContextFactory: ComponentContextFactory<CContext> =
      ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
        DefaultCContext(lifecycle, stateKeeper, instanceKeeper, backHandler, navigation)
      }
  override var navigation: StackNavigation<Any> = fakeStackNavigation
  override var messageShower: MessageShower = messageHolder

  fun resumed(): TestCContext {
    lifecycleRegistry.resume()
    return this
  }
}
