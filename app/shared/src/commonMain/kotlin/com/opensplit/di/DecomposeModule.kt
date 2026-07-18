package com.opensplit.di

import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.DefaultAuthComponent
import com.opensplit.features.auth.DefaultLoginComponent
import com.opensplit.features.auth.DefaultResetPasswordComponent
import com.opensplit.features.auth.DefaultSignUpComponent
import com.opensplit.features.auth.DefaultWelcomeComponent
import com.opensplit.features.auth.LoginComponent
import com.opensplit.features.auth.ResetPasswordComponent
import com.opensplit.features.auth.SignUpComponent
import com.opensplit.features.auth.WelcomeComponent
import com.opensplit.features.expense.AddExpenseComponent
import com.opensplit.features.expense.DefaultAddExpenseComponent
import com.opensplit.features.household.createjoin.CreateHouseholdComponent
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponent
import com.opensplit.features.household.createjoin.DefaultCreateHouseholdComponent
import com.opensplit.features.household.createjoin.DefaultCreateJoinHouseholdComponent
import com.opensplit.features.household.details.DefaultHouseholdDetailsComponent
import com.opensplit.features.household.details.HouseholdDetailsComponent
import com.opensplit.features.household.my.DefaultMyHouseholdsListComponent
import com.opensplit.features.household.my.MyHouseholdsListComponent
import com.opensplit.features.household.settings.DefaultHouseholdSettingsComponent
import com.opensplit.features.household.settings.HouseholdSettingsComponent
import com.opensplit.root.ComponentProvider
import com.opensplit.root.DefaultRootComponent
import com.opensplit.root.KoinComponentProvider
import com.opensplit.root.RootComponent
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun decomposeModule() = module {
  factory { KoinComponentProvider(this) }.bind<ComponentProvider>()
  factoryOf(DefaultRootComponent::Factory).bind<RootComponent.Factory>()
  factoryOf(DefaultAuthComponent::Factory).bind<AuthComponent.Factory>()
  factoryOf(DefaultWelcomeComponent::Factory).bind<WelcomeComponent.Factory>()
  factoryOf(DefaultLoginComponent::Factory).bind<LoginComponent.Factory>()
  factoryOf(DefaultSignUpComponent::Factory).bind<SignUpComponent.Factory>()
  factoryOf(DefaultResetPasswordComponent::Factory).bind<ResetPasswordComponent.Factory>()
  factoryOf(DefaultCreateJoinHouseholdComponent::Factory)
      .bind<CreateJoinHouseholdComponent.Factory>()
  factoryOf(DefaultCreateHouseholdComponent::Factory).bind<CreateHouseholdComponent.Factory>()
  factoryOf(DefaultMyHouseholdsListComponent::Factory).bind<MyHouseholdsListComponent.Factory>()
  factoryOf(DefaultHouseholdDetailsComponent::Factory).bind<HouseholdDetailsComponent.Factory>()
  factoryOf(DefaultHouseholdSettingsComponent::Factory).bind<HouseholdSettingsComponent.Factory>()
  factoryOf(DefaultAddExpenseComponent::Factory).bind<AddExpenseComponent.Factory>()
}
