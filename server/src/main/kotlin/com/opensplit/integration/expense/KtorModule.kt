package com.opensplit.integration.expense

import io.ktor.server.application.Application

fun Application.expenseModule() {
  configureExpenseRoutes()
}
