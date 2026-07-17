package com.opensplit.features.household

import io.ktor.server.application.Application

fun Application.householdModule() {
  configureHouseholdRoutes()
}
