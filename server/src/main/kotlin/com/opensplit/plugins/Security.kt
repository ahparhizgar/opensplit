package com.opensplit.plugins

import com.opensplit.features.auth.JwtService
import com.opensplit.features.auth.UserPrincipal
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
  val jwtService by inject<JwtService>()

  install(Authentication) {
    jwt("user-jwt") {
      verifier(jwtService.verifier)
      validate { credential ->
        val userId = credential.payload.subject
        val email = credential.payload.getClaim("email").asString()
        val name = credential.payload.getClaim("name").asString()
        if (email != null) {
          UserPrincipal(userId, email, name)
        } else {
          null
        }
      }
    }
  }
}

fun RoutingCall.user(): UserPrincipal =
    checkNotNull(principal<UserPrincipal>()) { "call.user() called on a non-authenticated route." }
