package com.opensplit.routes

import com.opensplit.db.Households
import com.opensplit.db.Memberships
import com.opensplit.db.Users
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Application.householdRoutes() {
    routing {
        post("/households") {
            val req = call.receive<CreateHouseholdRequest>()
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val email = token?.substringAfterLast('-')
            val userId = if (email != null) transaction { Users.select { Users.email eq email }.limit(1).firstOrNull()?.get(Users.id) } else null

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Authentication required"))
                return@post
            }

            val householdId = UUID.randomUUID().toString()
            val inviteCode = UUID.randomUUID().toString().take(8)

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
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val email = token?.substringAfterLast('-')
            val userId = if (email != null) transaction { Users.select { Users.email eq email }.limit(1).firstOrNull()?.get(Users.id) } else null

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Authentication required"))
                return@post
            }

            var householdRow = transaction { Households.select { Households.inviteCode eq req.inviteCodeOrId }.limit(1).firstOrNull() }

            if (householdRow == null) {
                householdRow = transaction { Households.select { Households.id eq req.inviteCodeOrId }.limit(1).firstOrNull() }
            }

            if (householdRow == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Household not found"))
                return@post
            }

            val hid = transaction { householdRow.get(Households.id) }

            transaction {
                Memberships.insert {
                    it[Memberships.id] = UUID.randomUUID().toString()
                    it[Memberships.householdId] = hid
                    it[Memberships.userId] = userId
                }
            }

            call.respond(HttpStatusCode.OK, JoinHouseholdResponse(hid, true))
        }
    }
}
