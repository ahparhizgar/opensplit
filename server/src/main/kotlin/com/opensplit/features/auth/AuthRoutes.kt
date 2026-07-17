package com.opensplit.features.auth

import com.opensplit.db.Users
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.validation.auth.AuthValidation
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject

fun Application.authRoutes() {
  val authService: AuthService by inject()
  routing {
    post("/users") {
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
          } catch (_: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    generalError = "Account already exists",
                    errors = mapOf("email" to "This email already exists"),
                ),
            )
            return@post
          }

      // set cookie using Ktor cookies API with secure flags
      call.response.cookies.append(
          "opensplit-auth-session",
          session.accessToken,
          path = "/",
          httpOnly = true,
          secure = true,
      )

      call.respond(HttpStatusCode.Created, session)
    }

    post("/tokens") {
      val request = call.receive<SignInRequest>()
      val validation = AuthValidation.validateSignIn(request.email, request.password)
      if (!validation.isValid) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                generalError = "Invalid email or password",
                errors = validation.errors,
            ),
        )
        return@post
      }

      val session =
          try {
            authService.signIn(request.email, request.password)
          } catch (_: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse("Invalid email or password"),
            )
            return@post
          }

      // set cookie using Ktor cookies API with secure flags
      call.response.cookies.append(
          "opensplit-auth-session",
          session.accessToken,
          path = "/",
          httpOnly = true,
          secure = true,
      )

      call.respond(HttpStatusCode.OK, session)
    }

    get("/household-context") {
      val raw =
          call.request.headers["Authorization"]?.removePrefix("Bearer ")
              ?: call.request.cookies["opensplit-auth-session"]
      val token = raw?.let { java.net.URLDecoder.decode(it, "UTF-8") }
      if (token.isNullOrBlank()) {
        call.respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse("Sign in required"),
        )
        return@get
      }

      val userId = JwtTokenService.verify(token)
      val email =
          if (userId == null) null
          else
              transaction {
                Users.selectAll()
                    .where { Users.id eq userId }
                    .limit(1)
                    .firstOrNull()
                    ?.get(Users.email)
              }
      if (email == null) {
        call.respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse("Sign in required"),
        )
        return@get
      }
      call.respond(
          HttpStatusCode.OK,
          HouseholdContextState(
              authenticated = true,
              email = email,
              householdId = null,
              message = "Authenticated household context",
          ),
      )
    }
  }
}
