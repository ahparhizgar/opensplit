package com.opensplit.features.auth

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.testOpenSplit
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthScenarios {
  @Test
  fun signInRejectsInvalidData() = testOpenSplit {
    val response = client.post("/tokens") { setBody(SignInRequest("bad", "short")) }

    assertEquals(HttpStatusCode.BadRequest, response.status)
    val error: ErrorResponse = response.body()
    assertNotNull(error.errors["email"])
  }

  @Test
  fun cannotSignUpUsingTheSameEmail() = testOpenSplit {
    val signUp = client.post("/users") { setBody(SignUpRequest("new@example.com", "password123")) }

    assertEquals(HttpStatusCode.Created, signUp.status)

    val secondSignUp =
        client.post("/users") { setBody(SignUpRequest("new@example.com", "newPassword123")) }

    assertEquals(HttpStatusCode.Conflict, secondSignUp.status)
  }

  @Test
  fun cannotSignInUsingWrongPassword() = testOpenSplit {
    val signUp =
        client.post("/users") { setBody(SignUpRequest("wrong-pass@example.com", "password123")) }

    assertEquals(HttpStatusCode.Created, signUp.status)

    val signIn =
        client.post("/tokens") { setBody(SignInRequest("wrong-pass@example.com", "incorrect")) }

    assertEquals(HttpStatusCode.Unauthorized, signIn.status)
    val error: ErrorResponse = signIn.body()
    assertEquals("Invalid email or password", error.generalError)
  }

  @Test
  fun canSignInAfterSignUp() = testOpenSplit {
    val email = "fresh@example.com"
    val password = "password123"

    val signUp = client.post("/users") { setBody(SignUpRequest(email, password)) }

    assertEquals(HttpStatusCode.Created, signUp.status)

    val signIn = client.post("/tokens") { setBody(SignInRequest(email, password)) }

    assertEquals(HttpStatusCode.OK, signIn.status)
    val session: AuthSessionState = signIn.body()
    assertEquals(email, session.email)
    assertNotNull(session.accessToken)
    assertTrue(session.accessToken.isNotBlank())
  }
}
