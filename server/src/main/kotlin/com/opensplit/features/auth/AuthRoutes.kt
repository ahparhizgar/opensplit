package com.opensplit.features.auth

import com.opensplit.dto.auth.AuthErrorResponse
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
            var raw = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: call.request.cookies["opensplit-auth-session"]
            val token = raw?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            if (token.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized, AuthErrorResponse(mapOf("token" to "Sign in required")))
                return@get
            }

            val email = token.substringAfterLast('-')
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

fun Application.configureJwtAuth() {}
