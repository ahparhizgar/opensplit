package com.opensplit.features.auth

import com.opensplit.dto.auth.AuthErrorResponse
import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthRoutesTest {
    @Test
    fun signUpCreatesSessionAndReturnsCreated() = testOpenSplit {
        val response = client.post("/auth/sign-up") {
            setBody(SignUpRequest("new@example.com", "password123"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val session = response.body<AuthSessionState>()
        assertEquals("new@example.com", session.email)
        assertNotNull(response.headers[HttpHeaders.SetCookie])
    }

    @Test
    fun signInRejectsInvalidDataWithoutSession() = testOpenSplit {
        val response = client.post("/auth/sign-in") {
            setBody(SignInRequest("bad", "short"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error: AuthErrorResponse = response.body()
        assertNotNull(error.errors["email"])
        assertEquals(null, response.headers[HttpHeaders.SetCookie])
    }

    @Test
    fun signInAuthenticatesAndAllowsHouseholdContext() = testOpenSplit {
        val signUp = client.post("/auth/sign-up") {
            setBody(SignUpRequest("member@example.com", "password123"))
        }
        assertEquals(HttpStatusCode.Created, signUp.status)
        val cookie = signUp.headers[HttpHeaders.SetCookie]
            ?.substringBefore(';')
            ?.substringAfter('=')
        assertNotNull(cookie)

        val signIn = client.post("/auth/sign-in") {
            setBody(SignInRequest("member@example.com", "password123"))
        }
        assertEquals(HttpStatusCode.OK, signIn.status)

        val householdResponse = client.get("/household-context") {
            headers.append(HttpHeaders.Cookie, "opensplit-auth-session=$cookie")
        }
        assertEquals(HttpStatusCode.OK, householdResponse.status)

        val context = householdResponse.body<HouseholdContextState>()

        assertEquals(true, context.authenticated)
        assertEquals("member@example.com", context.email)
    }
}
