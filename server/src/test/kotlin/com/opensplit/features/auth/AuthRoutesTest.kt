package com.opensplit.features.auth

import com.opensplit.module
import com.opensplit.dto.auth.AuthErrorResponse
import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import io.ktor.client.request.setBody
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.client.statement.bodyAsText
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun signUpCreatesSessionAndReturnsCreated() = testApplication {
        application { module() }

        val response = client.post("/auth/sign-up") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(SignUpRequest.serializer(), SignUpRequest("new@example.com", "password123")))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val session = json.decodeFromString<com.opensplit.dto.auth.AuthSessionState>(response.bodyAsText())
        assertEquals("new@example.com", session.email)
        assertNotNull(response.headers[HttpHeaders.SetCookie])
    }

    @Test
    fun signInRejectsInvalidDataWithoutSession() = testApplication {
        application { module() }

        val response = client.post("/auth/sign-in") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(SignInRequest.serializer(), SignInRequest("bad", "short")))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = json.decodeFromString<com.opensplit.dto.auth.AuthErrorResponse>(response.bodyAsText())
        assertNotNull(error.errors["email"])
        assertEquals(null, response.headers[HttpHeaders.SetCookie])
    }

    @Test
    fun signInAuthenticatesAndAllowsHouseholdContext() = testApplication {
        application { module() }

        val signUp = client.post("/auth/sign-up") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(SignUpRequest.serializer(), SignUpRequest("member@example.com", "password123")))
        }
        assertEquals(HttpStatusCode.Created, signUp.status)
        val cookie = signUp.headers[HttpHeaders.SetCookie]
            ?.substringBefore(';')
            ?.substringAfter('=')
        assertNotNull(cookie)

        val signIn = client.post("/auth/sign-in") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(SignInRequest("member@example.com", "password123")))
        }
        assertEquals(HttpStatusCode.OK, signIn.status)

        val householdResponse = client.get("/household-context") {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Cookie, "opensplit-auth-session=$cookie")
        }
        assertEquals(HttpStatusCode.OK, householdResponse.status)

        val context = json.decodeFromString<com.opensplit.dto.auth.HouseholdContextState>(householdResponse.bodyAsText())

        assertEquals(true, context.authenticated)
        assertEquals("member@example.com", context.email)
    }
}
