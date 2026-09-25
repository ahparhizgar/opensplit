package com.opensplit.di

import com.opensplit.integration.auth.AuthComponentFactory
import com.opensplit.integration.auth.DefaultAuthComponentFactory
import com.opensplit.integration.auth.DefaultLoginComponentFactory
import com.opensplit.integration.auth.DefaultResetPasswordComponentFactory
import com.opensplit.integration.auth.DefaultSignUpComponentFactory
import com.opensplit.integration.auth.DefaultWelcomeComponentFactory
import com.opensplit.integration.auth.LoginComponentFactory
import com.opensplit.integration.auth.ResetPasswordComponentFactory
import com.opensplit.integration.auth.SignUpComponentFactory
import com.opensplit.integration.auth.WelcomeComponentFactory
import com.opensplit.integration.expense.AddExpenseComponentFactory
import com.opensplit.integration.expense.AddExpenseFlowComponentFactory
import com.opensplit.integration.expense.DefaultAddExpenseComponentFactory
import com.opensplit.integration.expense.DefaultAddExpenseFlowComponentFactory
import com.opensplit.integration.expense.DefaultExpenseDetailsComponentFactory
import com.opensplit.integration.expense.DefaultMoreSplitOptionsComponentFactory
import com.opensplit.integration.expense.DefaultPaidAmountsComponentFactory
import com.opensplit.integration.expense.DefaultPayerFlowComponentFactory
import com.opensplit.integration.expense.DefaultQuickSplitComponentFactory
import com.opensplit.integration.expense.DefaultSplitFlowComponentFactory
import com.opensplit.integration.expense.DefaultWhoPaidComponentFactory
import com.opensplit.integration.expense.ExpenseDetailsComponentFactory
import com.opensplit.integration.expense.MoreSplitOptionsComponentFactory
import com.opensplit.integration.expense.PaidAmountsComponentFactory
import com.opensplit.integration.expense.PayerFlowComponentFactory
import com.opensplit.integration.expense.QuickSplitComponentFactory
import com.opensplit.integration.expense.SplitFlowComponentFactory
import com.opensplit.integration.expense.WhoPaidComponentFactory
import com.opensplit.integration.group.createjoin.CreateGroupComponentFactory
import com.opensplit.integration.group.createjoin.DefaultCreateGroupComponentFactory
import com.opensplit.integration.group.createjoin.DefaultGroupSelectionComponentFactory
import com.opensplit.integration.group.createjoin.DefaultJoinGroupComponentFactory
import com.opensplit.integration.group.createjoin.GroupSelectionComponentFactory
import com.opensplit.integration.group.createjoin.JoinGroupComponentFactory
import com.opensplit.integration.group.details.DefaultGroupDetailsComponentFactory
import com.opensplit.integration.group.details.DefaultGroupFlowComponentFactory
import com.opensplit.integration.group.details.GroupDetailsComponentFactory
import com.opensplit.integration.group.details.GroupFlowComponentFactory
import com.opensplit.integration.group.my.DefaultMyGroupsListComponentFactory
import com.opensplit.integration.group.my.MyGroupsListComponentFactory
import com.opensplit.integration.group.settings.DefaultGroupSettingsComponentFactory
import com.opensplit.integration.group.settings.GroupSettingsComponentFactory
import com.opensplit.integration.profile.DefaultProfileComponentFactory
import com.opensplit.integration.profile.ProfileComponentFactory
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
