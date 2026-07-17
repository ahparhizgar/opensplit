package com.opensplit.features.household

import com.opensplit.common.readAccessToken
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.features.auth.AuthService
import com.opensplit.features.auth.AuthUser
import com.opensplit.validation.household.HouseholdValidation
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureHouseholdRoutes() {
  val authService: AuthService by dependencies
  val householdService: HouseholdService by dependencies

  routing {
    route("/households") {
      get {
        val user = call.authenticatedUser(authService) ?: return@get
        call.respond(HttpStatusCode.OK, householdService.loadOverview(user))
      }

      post {
        val request = call.receive<CreateHouseholdRequest>()
        val validation = HouseholdValidation.validateCreateHousehold(request.name)
        if (!validation.isValid) {
          val fieldError = validation.errors["name"]
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse(
                  generalError = "Invalid household name",
                  errors = if (fieldError == null) emptyMap() else mapOf("name" to fieldError),
              ),
          )
          return@post
        }

        val user = call.authenticatedUser(authService) ?: return@post
        call.respond(HttpStatusCode.Created, householdService.createHousehold(user, request.name))
      }

      route("/memberships") {
        post {
          val user = call.authenticatedUser(authService) ?: return@post
          when (val request = call.receive<JoinHouseholdRequest>()) {
            is JoinHouseholdRequest.ByInvite -> {
              val validation =
                  HouseholdValidation.validateJoinHousehold(request.inviteCodeOrIdOrLink)
              if (!validation.isValid) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(generalError = "Invite code is required"),
                )
                return@post
              }

              when (
                  val result = householdService.joinHousehold(user, request.inviteCodeOrIdOrLink)
              ) {
                is JoinHouseholdResult.Success -> call.respond(HttpStatusCode.OK, result.household)
                JoinHouseholdResult.InvalidInviteCode ->
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(
                            generalError = "Invalid invite code",
                            errors = mapOf("inviteCode" to "Invalid invite code."),
                        ),
                    )
                JoinHouseholdResult.MissingPermission ->
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse(
                            generalError = "Missing permission to access this household",
                            errors =
                                mapOf(
                                    "permission" to "Missing permission to access this household"
                                ),
                        ),
                    )
              }
            }

            is JoinHouseholdRequest.ByEmail -> {
              if (request.email.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(generalError = "Email is required"),
                )
                return@post
              }

              when (
                  val result =
                      householdService.addMemberByEmail(user, request.householdId, request.email)
              ) {
                is AddMemberByEmailResult.Success ->
                    call.respond(HttpStatusCode.OK, result.household)
                AddMemberByEmailResult.HouseholdNotFound ->
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(generalError = "Household not found"),
                    )
                AddMemberByEmailResult.Forbidden ->
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse(generalError = "Only the owner can add members by email"),
                    )
                is AddMemberByEmailResult.UserNotFound ->
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(
                            generalError = "User with email ${result.email} not found",
                        ),
                    )
              }
            }
          }
        }
      }

      route("/{householdId}") {
        route("/memberships") {
          delete {
            val user = call.authenticatedUser(authService) ?: return@delete
            val householdId = call.parameters["householdId"]
            if (householdId.isNullOrBlank()) {
              call.respond(
                  HttpStatusCode.BadRequest,
                  ErrorResponse(
                      generalError = "Household id is required",
                      errors = mapOf("householdId" to "Household id is required"),
                  ),
              )
              return@delete
            }

            call.respond(HttpStatusCode.OK, householdService.leaveHousehold(user, householdId))
          }
        }
      }

      route("/{id}") {
        get {
          val householdId = call.parameters["id"]
          if (householdId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
          }

          val user = call.authenticatedUser(authService) ?: return@get
          val household = householdService.getHousehold(user, householdId)
          if (household == null) {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    generalError = "Household not found or access denied",
                    errors = mapOf("id" to "Household not found or access denied"),
                ),
            )
          } else {
            call.respond(HttpStatusCode.OK, household)
          }
        }
      }
    }
  }
}

private suspend fun ApplicationCall.authenticatedUser(authService: AuthService): AuthUser? {
  val user = authService.authenticatedUser(readAccessToken())
  if (user == null) {
    respond(
        HttpStatusCode.Unauthorized,
        ErrorResponse(
            generalError = "Authentication required",
            errors = mapOf("token" to "Authentication required"),
        ),
    )
  }
  return user
}
