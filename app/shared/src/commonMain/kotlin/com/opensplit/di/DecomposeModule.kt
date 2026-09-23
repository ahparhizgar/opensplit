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
import com.opensplit.features.expense.AddExpenseFlowComponentFactory
import com.opensplit.features.expense.DefaultAddExpenseComponentFactory
import com.opensplit.features.expense.DefaultAddExpenseFlowComponentFactory
import com.opensplit.features.expense.DefaultExpenseDetailsComponentFactory
import com.opensplit.features.expense.DefaultMoreSplitOptionsComponentFactory
import com.opensplit.features.expense.DefaultPaidAmountsComponentFactory
import com.opensplit.features.expense.DefaultPayerFlowComponentFactory
import com.opensplit.features.expense.DefaultQuickSplitComponentFactory
import com.opensplit.features.expense.DefaultSplitFlowComponentFactory
import com.opensplit.features.expense.DefaultWhoPaidComponentFactory
import com.opensplit.features.expense.ExpenseDetailsComponentFactory
import com.opensplit.features.expense.MoreSplitOptionsComponentFactory
import com.opensplit.features.expense.PaidAmountsComponentFactory
import com.opensplit.features.expense.PayerFlowComponentFactory
import com.opensplit.features.expense.QuickSplitComponentFactory
import com.opensplit.features.expense.SplitFlowComponentFactory
import com.opensplit.features.expense.WhoPaidComponentFactory
import com.opensplit.features.group.createjoin.CreateGroupComponentFactory
import com.opensplit.features.group.createjoin.DefaultCreateGroupComponentFactory
import com.opensplit.features.group.createjoin.DefaultGroupSelectionComponentFactory
import com.opensplit.features.group.createjoin.DefaultJoinGroupComponentFactory
import com.opensplit.features.group.createjoin.GroupSelectionComponentFactory
import com.opensplit.features.group.createjoin.JoinGroupComponentFactory
import com.opensplit.features.group.details.DefaultGroupDetailsComponentFactory
import com.opensplit.features.group.details.DefaultGroupFlowComponentFactory
import com.opensplit.features.group.details.GroupDetailsComponentFactory
import com.opensplit.features.group.details.GroupFlowComponentFactory
import com.opensplit.features.group.my.DefaultMyGroupsListComponentFactory
import com.opensplit.features.group.my.MyGroupsListComponentFactory
import com.opensplit.features.group.settings.DefaultGroupSettingsComponentFactory
import com.opensplit.features.group.settings.GroupSettingsComponentFactory
import com.opensplit.features.profile.DefaultProfileComponentFactory
import com.opensplit.features.profile.ProfileComponentFactory
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
  factoryOf(::DefaultGroupSelectionComponentFactory).bind<GroupSelectionComponentFactory>()
  factoryOf(::DefaultCreateGroupComponentFactory).bind<CreateGroupComponentFactory>()
  factoryOf(::DefaultJoinGroupComponentFactory).bind<JoinGroupComponentFactory>()
  factoryOf(::DefaultMyGroupsListComponentFactory).bind<MyGroupsListComponentFactory>()
  factoryOf(::DefaultGroupDetailsComponentFactory).bind<GroupDetailsComponentFactory>()
  factoryOf(::DefaultGroupFlowComponentFactory).bind<GroupFlowComponentFactory>()
  factoryOf(::DefaultGroupSettingsComponentFactory).bind<GroupSettingsComponentFactory>()
  factoryOf(::DefaultAddExpenseComponentFactory).bind<AddExpenseComponentFactory>()
  factoryOf(::DefaultExpenseDetailsComponentFactory).bind<ExpenseDetailsComponentFactory>()
  factoryOf(::DefaultMoreSplitOptionsComponentFactory).bind<MoreSplitOptionsComponentFactory>()
  factoryOf(::DefaultWhoPaidComponentFactory).bind<WhoPaidComponentFactory>()
  factoryOf(::DefaultQuickSplitComponentFactory).bind<QuickSplitComponentFactory>()
  factoryOf(::DefaultProfileComponentFactory).bind<ProfileComponentFactory>()
  factoryOf(::DefaultAddExpenseFlowComponentFactory).bind<AddExpenseFlowComponentFactory>()
  factoryOf(::DefaultPayerFlowComponentFactory).bind<PayerFlowComponentFactory>()
  factoryOf(::DefaultSplitFlowComponentFactory).bind<SplitFlowComponentFactory>()
  factoryOf(::DefaultPaidAmountsComponentFactory).bind<PaidAmountsComponentFactory>()
}
