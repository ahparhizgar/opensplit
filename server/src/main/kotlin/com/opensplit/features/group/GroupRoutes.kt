package com.opensplit.features.group

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.group.AddMemberByEmailRequest
import com.opensplit.dto.group.CreateGroupRequest
import com.opensplit.dto.group.GroupDto
import com.opensplit.dto.group.JoinGroupRequest
import com.opensplit.plugins.authenticateUser
import com.opensplit.plugins.user
import com.opensplit.validation.group.GroupValidation
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureGroupRoutes() {
  val groupService by inject<GroupService>()

  routing {
    authenticateUser {
      route("/groups") {
        get { call.respond(HttpStatusCode.OK, groupService.loadGroups(call.user())) }

        post {
          val request = call.receive<CreateGroupRequest>()
          val validation = GroupValidation.validateCreateGroup(request.name)
          if (!validation.isValid) {
            val fieldError = validation.errors["name"]
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    generalError = "Invalid group name",
                    errors = if (fieldError == null) emptyMap() else mapOf("name" to fieldError),
                ),
            )
            return@post
          }

          val user = call.user()
          call.respond<GroupDto>(
              HttpStatusCode.Created,
              groupService.createGroup(user, request.name),
          )
        }

        route("/memberships") {
          post {
            val user = call.user()
            val request = call.receive<JoinGroupRequest>()
            val validation = GroupValidation.validateJoinGroup(request.inviteCodeOrIdOrLink)
            if (!validation.isValid) {
              call.respond(
                  HttpStatusCode.BadRequest,
                  ErrorResponse(generalError = "Invite code is required"),
              )
              return@post
            }

            when (val result = groupService.joinGroup(user, request.inviteCodeOrIdOrLink)) {
              JoinGroupResult.InvalidInviteCode ->
                  call.respond(
                      HttpStatusCode.NotFound,
                      ErrorResponse(
                          generalError = "Invalid invite code",
                          errors = mapOf("inviteCode" to "Invalid invite code."),
                      ),
                  )
              JoinGroupResult.MissingPermission ->
                  call.respond(
                      HttpStatusCode.Forbidden,
                      ErrorResponse(
                          generalError = "Missing permission to access this group",
                          errors = mapOf("permission" to "Missing permission to access this group"),
                      ),
                  )
              is JoinGroupResult.Success -> call.respond<GroupDto>(HttpStatusCode.OK, result.group)
            }
          }
        }

        route("/{groupId}") {
          route("/memberships") {
            post {
              val user = call.user()
              val groupId = call.parameters["groupId"]
              if (groupId.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(generalError = "Group id is required"),
                )
                return@post
              }

              val request = call.receive<AddMemberByEmailRequest>()
              if (request.email.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(generalError = "Email is required"),
                )
                return@post
              }

              when (val result = groupService.addMemberByEmail(user, groupId, request.email)) {
                AddMemberByEmailResult.GroupNotFound ->
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(generalError = "Group not found"),
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
                is AddMemberByEmailResult.Success ->
                    call.respond<GroupDto>(HttpStatusCode.OK, result.group)
              }
            }
            delete {
              val user = call.user()
              val groupId = call.parameters["groupId"]
              if (groupId.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        generalError = "Group id is required",
                        errors = mapOf("groupId" to "Group id is required"),
                    ),
                )
                return@delete
              }

              call.respond(HttpStatusCode.OK, groupService.leaveGroup(user, groupId))
            }
          }
        }

        route("/{id}") {
          get {
            val groupId = call.parameters["id"]
            if (groupId.isNullOrBlank()) {
              call.respond(HttpStatusCode.BadRequest)
              return@get
            }

            val user = call.user()
            val group = groupService.getGroup(user, groupId)
            if (group == null) {
              call.respond(
                  HttpStatusCode.NotFound,
                  ErrorResponse(
                      generalError = "Group not found or access denied",
                      errors = mapOf("id" to "Group not found or access denied"),
                  ),
              )
            } else {
              call.respond<GroupDto>(HttpStatusCode.OK, group)
            }
          }
        }
      }
    }
  }
}
