package com.opensplit.routes

import com.opensplit.db.Households
import com.opensplit.db.Memberships
import com.opensplit.db.Users
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.NewHouseholdDto
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
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

fun Application.householdRoutes() {
    routing {
        post("/households") {
            val req = call.receive<CreateHouseholdRequest>()

            if (req.name.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        generalError = "Invalid household name",
                        errors = mapOf("name" to "Name must not be empty")
                    ),
                )
                return@post
            }
            if (req.name.length > 255) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        generalError = "Invalid household name",
                        errors = mapOf("name" to "Name is too long")
                    ),
                )
                return@post
            }

            val raw = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: call.request.cookies["opensplit-auth-session"]
            val token = raw?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            val userId = resolveUserIdFromToken(token)

            if (userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        generalError = "Authentication required",
                        errors = mapOf("token" to "Authentication required")
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

            call.respond(HttpStatusCode.Created, NewHouseholdDto(householdId, req.name, inviteCode))
        }

        post("/households/join") {
            val req = call.receive<JoinHouseholdRequest>()
            if (req.inviteCodeOrId.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        generalError = "Invite code is required",
                        errors = mapOf("inviteCodeOrId" to "Invite code is required")
                    ),
                )
                return@post
            }

            val raw = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: call.request.cookies["opensplit-auth-session"]
            val token = raw?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            val userId = resolveUserIdFromToken(token)

            if (userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        generalError = "Authentication required",
                        errors = mapOf("token" to "Authentication required")
                    ),
                )
                return@post
            }

            val byInvite = transaction {
                Households.select { Households.inviteCode eq req.inviteCodeOrId }.limit(1)
                    .firstOrNull()
            }
            val byId = if (byInvite == null) transaction {
                Households.select { Households.id eq req.inviteCodeOrId }.limit(1).firstOrNull()
            } else null
            val householdRow = byInvite ?: byId

            if (householdRow == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        generalError = "Invalid invite code or household id",
                        errors = mapOf("inviteCodeOrId" to "Invalid invite code or household id"),
                    ),
                )
                return@post
            }

            val hid = transaction { householdRow.get(Households.id) }
            val isJoinByInvite = byInvite != null

            if (!isJoinByInvite) {
                val ownerId = transaction { householdRow.get(Households.ownerId) }
                val isMember = transaction {
                    Memberships.select { (Memberships.householdId eq hid) and (Memberships.userId eq userId) }
                        .any()
                }
                if (ownerId != userId && !isMember) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse(
                            generalError = "Missing permission to access this household",
                            errors = mapOf("permission" to "Missing permission to access this household"),
                        ),
                    )
                    return@post
                }
            }

            transaction {
                val alreadyMember =
                    Memberships.select { (Memberships.householdId eq hid) and (Memberships.userId eq userId) }
                        .any()
                if (!alreadyMember) {
                    Memberships.insert {
                        it[Memberships.id] = UUID.randomUUID().toString()
                        it[Memberships.householdId] = hid
                        it[Memberships.userId] = userId
                    }
                }
            }

            // Todo return household overview
            call.respond(HttpStatusCode.OK)
        }

        get("/households/overview") {
            val raw = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: call.request.cookies["opensplit-auth-session"]
            val token = raw?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            val userId = resolveUserIdFromToken(token)
            if (userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        generalError = "Authentication required",
                        errors = mapOf("token" to "Authentication required")
                    )
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, loadOverviewForUser(userId))
        }

        delete("/households/{householdId}/memberships/me") {
            val householdId = call.parameters["householdId"]
            val raw = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: call.request.cookies["opensplit-auth-session"]
            val token = raw?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            val userId = resolveUserIdFromToken(token)
            if (userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        generalError = "Authentication required",
                        errors = mapOf("token" to "Authentication required")
                    )
                )
                return@delete
            }
            if (householdId.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        generalError = "Household id is required",
                        errors = mapOf("householdId" to "Household id is required")
                    )
                )
                return@delete
            }

            transaction {
                val household =
                    Households.select { Households.id eq householdId }.limit(1).firstOrNull()
                if (household != null && household[Households.ownerId] == userId) {
                    val nextOwnerQuery = Memberships.select {
                        (Memberships.householdId eq householdId) and (Memberships.userId neq userId)
                    }.limit(1).firstOrNull()
                    if (nextOwnerQuery != null) {
                        val nextUserId = nextOwnerQuery[Memberships.userId]
                        Households.update({ Households.id eq householdId }) {
                            it[Households.ownerId] = nextUserId
                        }
                    }
                }

                Memberships.deleteWhere { (Memberships.householdId eq householdId) and (Memberships.userId eq userId) }
            }

            call.respond(HttpStatusCode.OK, loadOverviewForUser(userId))
        }
    }
}

private fun resolveUserIdFromToken(token: String?): String? {
    val userId = token?.let { JwtTokenService.verify(it) } ?: return null
    return transaction { Users.select { Users.id eq userId }.limit(1).firstOrNull()?.get(Users.id) }
}

private fun loadOverviewForUser(userId: String): HouseholdOverviewDto {
    val households = transaction {
        Memberships.select { Memberships.userId eq userId }.map { membership ->
            val hid = membership[Memberships.householdId]
            val row = Households.select { Households.id eq hid }.limit(1).first()
            val memberCount = Memberships.select { Memberships.householdId eq hid }.count().toInt()
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
