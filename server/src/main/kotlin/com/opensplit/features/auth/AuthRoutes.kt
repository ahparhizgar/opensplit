package com.opensplit.features.auth

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.validation.auth.AuthValidation
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureAuthRoutes() {
  val authService by inject<AuthService>()

  routing {
    route("/users") {
      post {
        val request = call.receive<SignUpRequest>()
        val validation = AuthValidation.validateSignUp(request.email, request.password)
        if (!validation.isValid) {
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse(generalError = "Invalid sign up request", errors = validation.errors),
          )
          return@post
        }

        val session =
            try {
              authService.signUp(request.email, request.password, request.name)
            } catch (_: DuplicateEmailException) {
              call.respond(
                  HttpStatusCode.Conflict,
                  ErrorResponse(
                      generalError = "Account already exists",
                      errors = mapOf("email" to "This email already exists"),
                  ),
              )
              return@post
            }

        call.respond(HttpStatusCode.Created, session)
      }
    }

    route("/tokens") {
      post {
        val request = call.receive<SignInRequest>()
        val validation = AuthValidation.validateSignIn(request.email, request.password)
        if (!validation.isValid) {
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse(generalError = "Invalid email or password", errors = validation.errors),
          )
          return@post
        }

        val session =
            try {
              authService.signIn(request.email, request.password)
            } catch (_: InvalidCredentialsException) {
              call.respond(
                  HttpStatusCode.Unauthorized,
                  ErrorResponse(generalError = "Invalid email or password"),
              )
              return@post
            }

        call.respond(HttpStatusCode.OK, session)
      }
    }
  }
}
