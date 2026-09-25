package com.opensplit.util

import com.opensplit.database.ChangeLog
import com.opensplit.database.ExpenseParticipants
import com.opensplit.database.Expenses
import com.opensplit.database.Groups
import com.opensplit.database.Memberships
import com.opensplit.database.Users
import com.opensplit.dto.auth.AuthResult
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.integration.auth.AuthService
import com.opensplit.openSplit
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.ClientProvider
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject

fun testOpenSplit(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
  var token = ""
  application {
    openSplit(isTest = true)

    transaction {
      ExpenseParticipants.deleteAll()
      Expenses.deleteAll()
      Memberships.deleteAll()
      Groups.deleteAll()
      Users.deleteAll()
      ChangeLog.deleteAll()
    }

    val authService by inject<AuthService>()
    val auth = authService.signUp("registersdf@example.com", "password", "Amir")
    token = auth.accessToken
  }
  startApplication()
  client = createClientByToken(token)
  block()
}

@Deprecated("use createClient instead")
fun ClientProvider.createClientByToken(token: String): HttpClient = createClient {
  install(ContentNegotiation) { json() }

  install(DefaultRequest) { contentType(ContentType.Application.Json) }

  install(Auth) {
    bearer {
      cacheTokens = false
      loadTokens { BearerTokens(accessToken = token, refreshToken = null) }
    }
  }
}

suspend fun ClientProvider.createClient(name: String = "Other"): HttpClient {
  return createClientWithResult(name).first
}

suspend fun ClientProvider.createClientWithResult(
    name: String = "Other"
): Pair<HttpClient, AuthResult> {
  val otherUser =
      client
          .post("/users") {
            setBody(SignUpRequest("${name.lowercase()}@example.com", "password123", name))
          }
          .body<AuthResult>()
  return Pair(createClientByToken(otherUser.accessToken), otherUser)
}
