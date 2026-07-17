package com.opensplit.features.household

import com.opensplit.db.Households
import com.opensplit.db.Memberships
import com.opensplit.db.Users
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.HouseholdDto
import com.opensplit.dto.household.HouseholdMemberDto
import com.opensplit.dto.household.HouseholdOverviewDto
import com.opensplit.dto.household.HouseholdSummaryDto
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.features.auth.JwtTokenService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.net.URLDecoder
import java.util.UUID
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

fun Application.householdRoutes() {
  routing {
    route("/households") {
      get {
        val raw =
            call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: call.request.cookies["opensplit-auth-session"]
        val token = raw?.let { URLDecoder.decode(it, "UTF-8") }
        val userId = resolveUserIdFromToken(token)
        if (userId == null) {
          call.respond(
              HttpStatusCode.Unauthorized,
              ErrorResponse(
                  generalError = "Authentication required",
                  errors = mapOf("token" to "Authentication required"),
              ),
          )
          return@get
        }

        call.respond(HttpStatusCode.OK, loadHouseholds(userId))
      }

      post {
        val req = call.receive<CreateHouseholdRequest>()

        if (req.name.isBlank()) {
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse(
                  generalError = "Invalid household name",
                  errors = mapOf("name" to "Name must not be empty"),
              ),
          )
          return@post
        }
        if (req.name.length > 255) {
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse(
                  generalError = "Invalid household name",
                  errors = mapOf("name" to "Name is too long"),
              ),
          )
          return@post
        }

        val raw =
            call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: call.request.cookies["opensplit-auth-session"]
        val token = raw?.let { URLDecoder.decode(it, "UTF-8") }
        val userId = resolveUserIdFromToken(token)

        if (userId == null) {
          call.respond(
              HttpStatusCode.Unauthorized,
              ErrorResponse(
                  generalError = "Authentication required",
                  errors = mapOf("token" to "Authentication required"),
              ),
          )
          return@post
        }

        val householdId = UUID.randomUUID().toString()
        val inviteCode = UUID.randomUUID().toString().replace("-", "").take(12)

        transaction {
          Households.insert {
            it[Households.id] = householdId
            it[Households.name] = req.name
            it[Households.ownerId] = userId
            it[Households.inviteCode] = inviteCode
          }
          Memberships.insert {
            it[Memberships.id] = UUID.randomUUID().toString()
            it[Memberships.householdId] = householdId
            it[Memberships.userId] = userId
          }
        }

        call.respond(
            status = HttpStatusCode.Created,
            message =
                HouseholdDto(
                    id = householdId,
                    name = req.name,
                    members =
                        listOf(
                            HouseholdMemberDto(
                                userId = userId,
                                name =
                                    transaction {
                                      Users.selectAll()
                                          .where { Users.id eq userId }
                                          .first()[Users.name]
                                    },
                                email =
                                    transaction {
                                      Users.selectAll()
                                          .where { Users.id eq userId }
                                          .first()[Users.email]
                                    },
                                isOwner = true,
                                isCurrentUser = true,
                                balance = 0.0,
                                balanceCurrency = "IRR",
                            )
                        ),
                    inviteLink = "https://opensplit.com/join/$inviteCode",
                ),
        )
      }

      post("/memberships") {
        val req = call.receive<JoinHouseholdRequest>()
        val raw =
            call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: call.request.cookies["opensplit-auth-session"]
        val token = raw?.let { URLDecoder.decode(it, "UTF-8") }
        val userId = resolveUserIdFromToken(token)

        if (userId == null) {
          call.respond(
              HttpStatusCode.Unauthorized,
              ErrorResponse(
                  generalError = "Authentication required",
                  errors = mapOf("token" to "Authentication required"),
              ),
          )
          return@post
        }

        when (req) {
          is JoinHouseholdRequest.ByInvite -> {
            val inviteCodeOrIdOrLink = req.inviteCodeOrIdOrLink
            if (inviteCodeOrIdOrLink.isBlank()) {
              call.respond(
                  HttpStatusCode.BadRequest,
                  ErrorResponse(generalError = "Invite code is required"),
              )
              return@post
            }

            val byInvite = transaction {
              Households.selectAll()
                  .where {
                    Households.inviteCode eq
                        inviteCodeOrIdOrLink.removePrefix("https://opensplit.com/join/")
                  }
                  .limit(1)
                  .firstOrNull()
            }
            val byId =
                if (byInvite == null)
                    transaction {
                      Households.selectAll()
                          .where { Households.id eq inviteCodeOrIdOrLink }
                          .limit(1)
                          .firstOrNull()
                    }
                else null
            val householdRow = byInvite ?: byId

            if (householdRow == null) {
              call.respond(
                  HttpStatusCode.NotFound,
                  ErrorResponse(
                      generalError = "Invalid invite code",
                      errors = mapOf("inviteCode" to "Invalid invite code."),
                  ),
              )
              return@post
            }

            val hid = transaction { householdRow.get(Households.id) }
            val isJoinByInvite = byInvite != null

            if (!isJoinByInvite) {
              val ownerId = transaction { householdRow.get(Households.ownerId) }
              val isMember = transaction {
                Memberships.selectAll()
                    .where { (Memberships.householdId eq hid) and (Memberships.userId eq userId) }
                    .any()
              }
              if (ownerId != userId && !isMember) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(
                        generalError = "Missing permission to access this household",
                        errors =
                            mapOf("permission" to "Missing permission to access this household"),
                    ),
                )
                return@post
              }
            }

            transaction {
              val alreadyMember =
                  Memberships.selectAll()
                      .where { (Memberships.householdId eq hid) and (Memberships.userId eq userId) }
                      .any()
              if (!alreadyMember) {
                Memberships.insert {
                  it[Memberships.id] = UUID.randomUUID().toString()
                  it[Memberships.householdId] = hid
                  it[Memberships.userId] = userId
                }
              }
            }

            val detail = loadHouseholdDetail(hid, userId)
            if (detail != null) {
              call.respond(HttpStatusCode.OK, detail)
            } else {
              call.respond(HttpStatusCode.InternalServerError)
            }
          }

          is JoinHouseholdRequest.ByEmail -> {
            val householdId = req.householdId
            val email = req.email

            if (email.isBlank()) {
              call.respond(
                  HttpStatusCode.BadRequest,
                  ErrorResponse(generalError = "Email is required"),
              )
              return@post
            }

            val result = transaction {
              val household =
                  Households.selectAll()
                      .where { Households.id eq householdId }
                      .limit(1)
                      .firstOrNull() ?: return@transaction "NOT_FOUND"

              if (household[Households.ownerId] != userId) {
                return@transaction "FORBIDDEN"
              }

              val targetUser =
                  Users.selectAll().where { Users.email eq email }.limit(1).firstOrNull()
                      ?: return@transaction "USER_NOT_FOUND"

              val targetUserId = targetUser[Users.id]
              val alreadyMember =
                  Memberships.selectAll()
                      .where {
                        (Memberships.householdId eq householdId) and
                            (Memberships.userId eq targetUserId)
                      }
                      .any()
              if (!alreadyMember) {
                Memberships.insert {
                  it[Memberships.id] = UUID.randomUUID().toString()
                  it[Memberships.householdId] = householdId
                  it[Memberships.userId] = targetUserId
                }
              }
              "OK"
            }

            when (result) {
              "NOT_FOUND" ->
                  call.respond(
                      HttpStatusCode.NotFound,
                      ErrorResponse(generalError = "Household not found"),
                  )

              "FORBIDDEN" ->
                  call.respond(
                      HttpStatusCode.Forbidden,
                      ErrorResponse(generalError = "Only the owner can add members by email"),
                  )

              "USER_NOT_FOUND" ->
                  call.respond(
                      HttpStatusCode.NotFound,
                      ErrorResponse(generalError = "User with email $email not found"),
                  )

              "OK" -> {
                val detail = loadHouseholdDetail(householdId, userId)
                if (detail != null) call.respond(HttpStatusCode.OK, detail)
                else call.respond(HttpStatusCode.InternalServerError)
              }
            }
          }
        }
      }

      delete("/{householdId}/memberships") {
        val householdId = call.parameters["householdId"]
        val raw =
            call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: call.request.cookies["opensplit-auth-session"]
        val token = raw?.let { URLDecoder.decode(it, "UTF-8") }
        val userId = resolveUserIdFromToken(token)
        if (userId == null) {
          call.respond(
              HttpStatusCode.Unauthorized,
              ErrorResponse(
                  generalError = "Authentication required",
                  errors = mapOf("token" to "Authentication required"),
              ),
          )
          return@delete
        }
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

        transaction {
          val household =
              Households.selectAll().where { Households.id eq householdId }.limit(1).firstOrNull()
          if (household != null && household[Households.ownerId] == userId) {
            val nextOwnerQuery =
                Memberships.selectAll()
                    .where {
                      (Memberships.householdId eq householdId) and (Memberships.userId neq userId)
                    }
                    .limit(1)
                    .firstOrNull()
            if (nextOwnerQuery != null) {
              val nextUserId = nextOwnerQuery[Memberships.userId]
              Households.update({ Households.id eq householdId }) {
                it[Households.ownerId] = nextUserId
              }
            }
          }

          Memberships.deleteWhere {
            (Memberships.householdId eq householdId) and (Memberships.userId eq userId)
          }
        }

        call.respond(HttpStatusCode.OK, loadHouseholds(userId))
      }

      get("/{id}") {
        val householdId =
            call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        val raw = call.request.headers["Authorization"]?.removePrefix("Bearer ")
        val token = raw?.let { URLDecoder.decode(it, "UTF-8") }
        val userId = resolveUserIdFromToken(token)

        if (userId == null) {
          call.respond(
              HttpStatusCode.Unauthorized,
              ErrorResponse(
                  generalError = "Authentication required",
                  errors = mapOf("token" to "Authentication required"),
              ),
          )
          return@get
        }

        val result = loadHouseholdDetail(householdId, userId)

        if (result == null) {
          call.respond(
              HttpStatusCode.NotFound,
              ErrorResponse(
                  generalError = "Household not found or access denied",
                  errors = mapOf("id" to "Household not found or access denied"),
              ),
          )
        } else {
          call.respond(HttpStatusCode.OK, result)
        }
      }
    }
  }
}

private fun resolveUserIdFromToken(token: String?): String? {
  val userId = token?.let { JwtTokenService.verify(it) } ?: return null
  return transaction {
    Users.selectAll().where { Users.id eq userId }.limit(1).firstOrNull()?.get(Users.id)
  }
}

private fun loadHouseholdDetail(householdId: String, userId: String): HouseholdDto? {
  return transaction {
    val isMember =
        Memberships.selectAll()
            .where { (Memberships.householdId eq householdId) and (Memberships.userId eq userId) }
            .any()

    if (!isMember) return@transaction null

    val householdRow =
        Households.selectAll().where { Households.id eq householdId }.limit(1).firstOrNull()
            ?: return@transaction null

    val memberIds =
        Memberships.selectAll()
            .where { Memberships.householdId eq householdId }
            .map { it[Memberships.userId] }

    val ownerId = householdRow[Households.ownerId]

    val members =
        Users.selectAll()
            .where { Users.id inList memberIds }
            .map { row ->
              HouseholdMemberDto(
                  userId = row[Users.id],
                  name = row[Users.name],
                  email = row[Users.email],
                  isOwner = row[Users.id] == ownerId,
                  isCurrentUser = row[Users.id] == userId,
                  balance = if (row[Users.id] == userId) 10.15 else -10.15,
                  balanceCurrency = "IRR",
              )
            }

    HouseholdDto(
        id = householdRow[Households.id],
        name = householdRow[Households.name],
        members = members,
        inviteLink = "https://opensplit.com/join/${householdRow[Households.inviteCode]}",
    )
  }
}

private fun loadHouseholds(userId: String): HouseholdOverviewDto {
  val households = transaction {
    Memberships.selectAll()
        .where { Memberships.userId eq userId }
        .map { membership ->
          val hid = membership[Memberships.householdId]
          val row = Households.selectAll().where { Households.id eq hid }.limit(1).first()
          val memberCount =
              Memberships.selectAll().where { Memberships.householdId eq hid }.count().toInt()
          val ownerId = row[Households.ownerId]
          HouseholdSummaryDto(
              id = row[Households.id],
              name = row[Households.name],
              memberCount = memberCount,
              isOwner = ownerId == userId,
              inviteCode = row[Households.inviteCode],
          )
        }
  }
  return HouseholdOverviewDto(households = households, members = emptyList())
}
