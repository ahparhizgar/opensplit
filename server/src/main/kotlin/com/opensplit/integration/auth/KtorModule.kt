package com.opensplit.integration.auth

import io.ktor.server.application.Application

fun Application.authModule() {
  configureAuthRoutes()
}
