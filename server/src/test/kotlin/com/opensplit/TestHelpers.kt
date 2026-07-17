package com.opensplit

import com.opensplit.database.Households
import com.opensplit.database.Memberships
import com.opensplit.database.Users
import com.opensplit.dto.auth.AuthResult
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.features.auth.AuthService
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
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun testOpenSplit(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
  var token = ""
  application {
    openSplit(isTest = true)

    transaction {
      Memberships.deleteAll()
      Households.deleteAll()
      Users.deleteAll()
    }

    val authService: AuthService by dependencies
    val auth = authService.signUp("registersdf@example.com", "password", null)
    token = auth.accessToken
  }
  startApplication()
  client = createTestClient(token)
  block()
}

fun ApplicationTestBuilder.createAuthenticatedClient(token: String): HttpClient = createClient {
  install(ContentNegotiation) { json() }

  install(DefaultRequest) { contentType(ContentType.Application.Json) }

  install(Auth) {
    bearer {
      cacheTokens = false
      loadTokens { BearerTokens(accessToken = token, refreshToken = null) }
    }
  }
}

private fun ApplicationTestBuilder.createTestClient(token: String): HttpClient =
    createAuthenticatedClient(token)

suspend fun ApplicationTestBuilder.createOtherClient(): HttpClient {
  val otherUser =
      client
          .post("/users") { setBody(SignUpRequest("other@example.com", "password123")) }
          .body<AuthResult>()
  return createAuthenticatedClient(otherUser.accessToken)
}
