package com.opensplit.routes

import com.opensplit.db.Households
import com.opensplit.db.Memberships
import com.opensplit.db.Users
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.household.CreateHouseholdRequest
import com.opensplit.dto.household.CreateHouseholdResponse
import com.opensplit.dto.household.JoinHouseholdRequest
import com.opensplit.dto.household.JoinHouseholdResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Application.householdRoutes() {
    routing {
        post("/households") {
            val req = call.receive<CreateHouseholdRequest>()

            if (req.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(errors = mapOf("name" to "Name must not be empty")))
                return@post
            }
            if (req.name.length > 255) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(errors = mapOf("name" to "Name is too long")))
                return@post
            }

            val raw = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: call.request.cookies["opensplit-auth-session"]
            val token = raw?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            val userId = resolveUserIdFromToken(token)

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(errors = mapOf("token" to "Authentication required")))
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

            call.respond(HttpStatusCode.Created, CreateHouseholdResponse(householdId, req.name, inviteCode))
        }

        post("/households/join") {
            val req = call.receive<JoinHouseholdRequest>()
            if (req.inviteCodeOrId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(errors = mapOf("inviteCodeOrId" to "Invite code is required")))
                return@post
            }

            val raw = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: call.request.cookies["opensplit-auth-session"]
            val token = raw?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            val userId = resolveUserIdFromToken(token)

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(errors = mapOf("token" to "Authentication required")))
                return@post
            }

            val byInvite = transaction { Households.select { Households.inviteCode eq req.inviteCodeOrId }.limit(1).firstOrNull() }
            val byId = if (byInvite == null) transaction { Households.select { Households.id eq req.inviteCodeOrId }.limit(1).firstOrNull() } else null
            val householdRow = byInvite ?: byId

            if (householdRow == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(errors = mapOf("inviteCodeOrId" to "Invalid invite code or household id")))
                return@post
            }

            val hid = transaction { householdRow.get(Households.id) }
            val isJoinByInvite = byInvite != null

            if (!isJoinByInvite) {
                val ownerId = transaction { householdRow.get(Households.ownerId) }
                val isMember = transaction {
                    Memberships.select { (Memberships.householdId eq hid) and (Memberships.userId eq userId) }.any()
                }
                if (ownerId != userId && !isMember) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(errors = mapOf("permission" to "Missing permission to access this household")))
                    return@post
                }
            }

            transaction {
                val alreadyMember = Memberships.select { (Memberships.householdId eq hid) and (Memberships.userId eq userId) }.any()
                if (!alreadyMember) {
                    Memberships.insert {
                        it[Memberships.id] = UUID.randomUUID().toString()
                        it[Memberships.householdId] = hid
                        it[Memberships.userId] = userId
                    }
                }
            }

            call.respond(HttpStatusCode.OK, JoinHouseholdResponse(hid, true))
        }
    }
}

private val jwtUserIdRegex = Regex("^jwt-([0-9a-fA-F-]{36})-")

private fun resolveUserIdFromToken(token: String?): String? {
    val userId = token?.let { jwtUserIdRegex.find(it)?.groupValues?.getOrNull(1) } ?: return null
    return transaction { Users.select { Users.id eq userId }.limit(1).firstOrNull()?.get(Users.id) }
}
