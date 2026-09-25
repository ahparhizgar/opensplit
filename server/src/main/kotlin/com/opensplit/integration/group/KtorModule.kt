package com.opensplit.integration.group

import io.ktor.server.application.Application

fun Application.groupModule() {
  configureGroupRoutes()
}
