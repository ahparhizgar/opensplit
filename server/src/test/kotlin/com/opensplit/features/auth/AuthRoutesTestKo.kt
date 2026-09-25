package com.opensplit.features.auth

import com.opensplit.KtorBehaviorSpec
import com.opensplit.dto.auth.AuthResult
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.testValue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

class AuthRoutesTestKo : KtorBehaviorSpec() {
  init {
    Given("authentication endpoints") {
      When("signing in with invalid email and password") {
        val response by testValue {
          client.post("/tokens") { setBody(SignInRequest("bad", "short")) }
        }
        Then("it returns 400 Bad Request with field validation errors") {
          response.status shouldBe HttpStatusCode.BadRequest
          val error: ErrorResponse = response.body()
          error.errors["email"].shouldNotBeNull()
        }
      }

      When("signing up and signing in with valid credentials") {
        val email by testValue { "fresh@example.com" }
        val password by testValue { "password123" }
        val name by testValue { "Amir" }
        val signUp by testValue {
          client.post("/users") { setBody(SignUpRequest(email, password, name)) }
        }
        Then("signs up successfully") {
          signUp.status shouldBe HttpStatusCode.Created

          val signIn = client.post("/tokens") { setBody(SignInRequest(email, password)) }
          signIn.status shouldBe HttpStatusCode.OK
          val session: AuthResult = signIn.body()
          session.email shouldBe email
          session.accessToken.shouldNotBeBlank()
        }

        And("trying to sign up with the same email again") {
          val duplicateSignUp by testValue {
            client.post("/users") { setBody(SignUpRequest(email, "newPassword123", name)) }
          }
          Then("it returns 409 Conflict") {
            duplicateSignUp.status shouldBe HttpStatusCode.Conflict
          }
        }
      }

      When("signing in with wrong password") {
        val email by testValue { "wrong-pass@example.com" }
        val password by testValue { "password123" }
        val name by testValue { "Amir" }
        val signUp by testValue {
          client.post("/users") { setBody(SignUpRequest(email, password, name)) }
        }
        val signIn by testValue {
          signUp // ensure user created
          client.post("/tokens") { setBody(SignInRequest(email, "incorrect")) }
        }
        Then("it returns 401 Unauthorized with error message") {
          signIn.status shouldBe HttpStatusCode.Unauthorized
          val error: ErrorResponse = signIn.body()
          error.generalError shouldBe "Invalid email or password"
        }
      }
    }
  }
}
