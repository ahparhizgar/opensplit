package com.opensplit.features.household

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.root.ComponentProvider
import com.opensplit.root.Destination
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.serialization.Serializable

interface HouseholdComponent : Destination {
    val childStack: Value<ChildStack<*, Child>>
    fun onHouseholdClick(id: String)

    sealed class Child {
        object Loading : Child()
        data class CreateJoin(val component: CreateJoinHouseholdComponent) : Child()
        data class List(val component: MyHouseholdsListComponent) : Child()
        data class Detail(val component: HouseholdDetailComponent) : Child()
    }

    @Serializable
    class Config : TopLevelDestinationConfig

    interface Factory {
        fun create(cContext: CContext, config: Config): HouseholdComponent
    }
}

class DefaultHouseholdComponent(
    context: CContext,
    private val componentProvider: ComponentProvider,
) : HouseholdComponent, CContext by context {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, HouseholdComponent.Child>> =
        childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = Config.List,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override fun onHouseholdClick(id: String) {
        navigation.pushNew(Config.Detail(id))
    }

    private fun createChild(config: Config, cContext: CContext): HouseholdComponent.Child {
        return when (config) {
            is Config.Loading -> HouseholdComponent.Child.Loading
            is Config.CreateJoin -> HouseholdComponent.Child.CreateJoin(
                componentProvider.provide(CreateJoinHouseholdComponent.Factory::class)
                    .create(cContext)
            )

            is Config.List -> HouseholdComponent.Child.List(
                componentProvider.provide(MyHouseholdsListComponent.Factory::class)
                    .create(cContext)
            )

            is Config.Detail -> HouseholdComponent.Child.Detail(
                componentProvider.provide(HouseholdDetailComponent.Factory::class)
                    .create(cContext, HouseholdDetailComponent.Config(config.householdId))
            )
        }
    }

    @Serializable
    sealed class Config {
        @Serializable
        object Loading : Config()

        @Serializable
        object CreateJoin : Config()

        @Serializable
        object List : Config()

        @Serializable
        data class Detail(val householdId: String) : Config()
    }

    class Factory(
        private val componentProvider: ComponentProvider,
    ) : HouseholdComponent.Factory {
        override fun create(
            cContext: CContext,
            config: HouseholdComponent.Config
        ): HouseholdComponent = DefaultHouseholdComponent(
            context = cContext,
            componentProvider = componentProvider
        )
    }
}

class FakeHouseholdComponent(
    override val childStack: MutableValue<ChildStack<*, HouseholdComponent.Child>> =
        MutableValue(
            ChildStack(
                active = com.arkivanov.decompose.Child.Created(
                    configuration = HouseholdComponent.Config(),
                    instance = HouseholdComponent.Child.Loading
                ),
                backStack = emptyList()
            )
        )
) : HouseholdComponent {
    override fun onHouseholdClick(id: String) {}
}
