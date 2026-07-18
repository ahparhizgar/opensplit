package com.opensplit.features.expense

import io.ktor.server.application.Application

fun Application.expenseModule() {
  configureExpenseRoutes()
}
