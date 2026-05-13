package com.opensplit.features.auth

import com.opensplit.dto.auth.AuthErrorResponse
import com.opensplit.dto.auth.AuthSessionState
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
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.get
import io.ktor.server.sessions.set

fun Application.authRoutes(authService: AuthService = AuthService()) {
    routing {
        post("/auth/sign-up") {
            val request = call.receive<SignUpRequest>()
            val validation = AuthValidation.validateSignUp(request.email, request.password)
            if (!validation.isValid) {
                call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(validation.errors))
                return@post
            }

            val session = try {
                authService.signUp(request.email, request.password)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, AuthErrorResponse(mapOf("email" to "Account already exists")))
                return@post
            }

            call.sessions.set(AuthSession(session.userId, session.email, session.householdId))
            call.respond(HttpStatusCode.Created, session)
        }

        post("/auth/sign-in") {
            val request = call.receive<SignInRequest>()
            val validation = AuthValidation.validateSignIn(request.email, request.password)
            if (!validation.isValid) {
                call.respond(HttpStatusCode.BadRequest, AuthErrorResponse(validation.errors))
                return@post
            }

            val session = try {
                authService.signIn(request.email, request.password)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, AuthErrorResponse(mapOf("password" to "Invalid email or password")))
                return@post
            }

            call.sessions.set(AuthSession(session.userId, session.email, session.householdId))
            call.respond(HttpStatusCode.OK, session)
        }

        get("/household-context") {
            val session = call.sessions.get<AuthSession>()
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthErrorResponse(mapOf("session" to "Sign in required")))
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                HouseholdContextState(
                    authenticated = true,
                    email = session.email,
                    householdId = session.householdId,
                    message = "Authenticated household context",
                ),
            )
        }
    }
}
