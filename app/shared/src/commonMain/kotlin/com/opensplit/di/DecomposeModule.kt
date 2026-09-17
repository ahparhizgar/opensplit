package com.opensplit.di

import com.opensplit.features.auth.AuthComponentFactory
import com.opensplit.features.auth.DefaultAuthComponentFactory
import com.opensplit.features.auth.DefaultLoginComponentFactory
import com.opensplit.features.auth.DefaultResetPasswordComponentFactory
import com.opensplit.features.auth.DefaultSignUpComponentFactory
import com.opensplit.features.auth.DefaultWelcomeComponentFactory
import com.opensplit.features.auth.LoginComponentFactory
import com.opensplit.features.auth.ResetPasswordComponentFactory
import com.opensplit.features.auth.SignUpComponentFactory
import com.opensplit.features.auth.WelcomeComponentFactory
import com.opensplit.features.expense.AddExpenseComponentFactory
import com.opensplit.features.expense.DefaultAddExpenseComponentFactory
import com.opensplit.features.expense.DefaultExpenseDetailsComponentFactory
import com.opensplit.features.expense.DefaultMoreSplitOptionsComponentFactory
import com.opensplit.features.expense.DefaultQuickSplitComponentFactory
import com.opensplit.features.expense.DefaultWhoPaidComponentFactory
import com.opensplit.features.expense.ExpenseDetailsComponentFactory
import com.opensplit.features.expense.MoreSplitOptionsComponentFactory
import com.opensplit.features.expense.QuickSplitComponentFactory
import com.opensplit.features.expense.WhoPaidComponentFactory
import com.opensplit.features.household.createjoin.CreateHouseholdComponentFactory
import com.opensplit.features.household.createjoin.CreateJoinHouseholdComponentFactory
import com.opensplit.features.household.createjoin.DefaultCreateHouseholdComponentFactory
import com.opensplit.features.household.createjoin.DefaultCreateJoinHouseholdComponentFactory
import com.opensplit.features.household.details.DefaultHouseholdDetailsComponentFactory
import com.opensplit.features.household.details.HouseholdDetailsComponentFactory
import com.opensplit.features.household.my.DefaultMyHouseholdsListComponentFactory
import com.opensplit.features.household.my.MyHouseholdsListComponentFactory
import com.opensplit.features.household.settings.DefaultHouseholdSettingsComponentFactory
import com.opensplit.features.household.settings.HouseholdSettingsComponentFactory
import com.opensplit.root.ComponentProvider
import com.opensplit.root.DefaultRootComponentFactory
import com.opensplit.root.KoinComponentProvider
import com.opensplit.root.RootComponentFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun decomposeModule() = module {
  factory { KoinComponentProvider(this) }.bind<ComponentProvider>()
  factoryOf(::DefaultRootComponentFactory).bind<RootComponentFactory>()
  factoryOf(::DefaultAuthComponentFactory).bind<AuthComponentFactory>()
  factoryOf(::DefaultWelcomeComponentFactory).bind<WelcomeComponentFactory>()
  factoryOf(::DefaultLoginComponentFactory).bind<LoginComponentFactory>()
  factoryOf(::DefaultSignUpComponentFactory).bind<SignUpComponentFactory>()
  factoryOf(::DefaultResetPasswordComponentFactory).bind<ResetPasswordComponentFactory>()
  factoryOf(::DefaultCreateJoinHouseholdComponentFactory)
      .bind<CreateJoinHouseholdComponentFactory>()
  factoryOf(::DefaultCreateHouseholdComponentFactory).bind<CreateHouseholdComponentFactory>()
  factoryOf(::DefaultMyHouseholdsListComponentFactory).bind<MyHouseholdsListComponentFactory>()
  factoryOf(::DefaultHouseholdDetailsComponentFactory).bind<HouseholdDetailsComponentFactory>()
  factoryOf(::DefaultHouseholdSettingsComponentFactory).bind<HouseholdSettingsComponentFactory>()
  factoryOf(::DefaultAddExpenseComponentFactory).bind<AddExpenseComponentFactory>()
  factoryOf(::DefaultExpenseDetailsComponentFactory).bind<ExpenseDetailsComponentFactory>()
  factoryOf(::DefaultMoreSplitOptionsComponentFactory).bind<MoreSplitOptionsComponentFactory>()
  factoryOf(::DefaultWhoPaidComponentFactory).bind<WhoPaidComponentFactory>()
  factoryOf(::DefaultQuickSplitComponentFactory).bind<QuickSplitComponentFactory>()
}
