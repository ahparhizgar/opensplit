package com.opensplit.features.auth

import io.ktor.server.application.Application

fun Application.authModule() {
  configureAuthRoutes()
}
