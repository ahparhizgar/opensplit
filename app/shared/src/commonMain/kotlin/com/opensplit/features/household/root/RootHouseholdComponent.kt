package com.opensplit.features.household.root

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.opensplit.component.CContext
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.features.household.my.MyHouseholdsListComponent
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.settings.HouseholdSettingsComponent
import com.opensplit.root.ComponentProvider
import com.opensplit.root.Destination
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.serialization.Serializable

interface RootHouseholdComponent : Destination {
    val childStack: Value<ChildStack<*, Child>>
    fun onHouseholdClick(id: String)

    sealed class Child {
        object Loading : Child()
        data class List(val component: MyHouseholdsListComponent) : Child()
        data class Detail(val component: HouseholdDetailsComponent) : Child()
        data class Settings(val component: HouseholdSettingsComponent) : Child()
    }

    @Serializable
    class Config : TopLevelDestinationConfig

    interface Factory {
        fun create(cContext: CContext, config: Config): RootHouseholdComponent
    }
}

class DefaultRootHouseholdComponent(
    context: CContext,
    private val componentProvider: ComponentProvider,
) : RootHouseholdComponent, CContext by context {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootHouseholdComponent.Child>> =
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

    private fun createChild(config: Config, cContext: CContext): RootHouseholdComponent.Child {
        return when (config) {
            is Config.Loading -> RootHouseholdComponent.Child.Loading

            is Config.List -> RootHouseholdComponent.Child.List(
                componentProvider.provide(MyHouseholdsListComponent.Factory::class)
                    .create(cContext)
            )

            is Config.Detail -> RootHouseholdComponent.Child.Detail(
                componentProvider.provide(HouseholdDetailsComponent.Factory::class)
                    .create(cContext, HouseholdDetailsComponent.Config(config.householdId))
            )

            is Config.Settings -> RootHouseholdComponent.Child.Settings(
                componentProvider.provide(HouseholdSettingsComponent.Factory::class)
                    .create(cContext, HouseholdSettingsComponent.Config(config.householdId))
            )
        }
    }

    @Serializable
    sealed class Config {
        @Serializable
        object Loading : Config()

        @Serializable
        object List : Config()

        @Serializable
        data class Detail(val householdId: String) : Config()

        @Serializable
        data class Settings(val householdId: String) : Config()
    }

    class Factory(
        private val componentProvider: ComponentProvider,
    ) : RootHouseholdComponent.Factory {
        override fun create(
            cContext: CContext,
            config: RootHouseholdComponent.Config
        ): RootHouseholdComponent = DefaultRootHouseholdComponent(
            context = cContext,
            componentProvider = componentProvider
        )
    }
}

class FakeRootHouseholdComponent(
    override val childStack: MutableValue<ChildStack<*, RootHouseholdComponent.Child>> =
        MutableValue(
            ChildStack(
                active = Child.Created(
                    configuration = RootHouseholdComponent.Config(),
                    instance = RootHouseholdComponent.Child.Loading
                ),
                backStack = emptyList()
            )
        )
) : RootHouseholdComponent {
    override fun onHouseholdClick(id: String) {}
}
