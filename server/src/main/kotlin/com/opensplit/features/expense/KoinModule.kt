package com.opensplit.features.expense

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun expenseKoinModule() = module {
  singleOf(::ExpenseRepositoryImpl).bind<ExpenseRepository>()
  singleOf(::ExpenseService)
}
