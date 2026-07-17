package com.opensplit.features.auth

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

data class AuthSubmissionResult(
    val session: AuthSessionState,
)

interface AuthApi{
  suspend fun signUp(email: String, password: String): AuthSubmissionResult

  suspend fun signIn(email: String, password: String): AuthSubmissionResult
}

class KtorAuthApi(
    private val client: HttpClient,
) : AuthApi {
  override suspend fun signUp(email: String, password: String): AuthSubmissionResult =
      submit(
          path = "/users",
          request = SignUpRequest(email = email, password = password),
      )

  override suspend fun signIn(email: String, password: String): AuthSubmissionResult =
      submit(
          path = "/tokens",
          request = SignInRequest(email = email, password = password),
      )

  private suspend fun submit(path: String, request: Any): AuthSubmissionResult {
    val response =
        client.post(path) {
          contentType(ContentType.Application.Json)
          setBody(request)
        }

    if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.Created) {
      response.body<ErrorResponse>()
    }

    val session = response.body<AuthSessionState>()
    return AuthSubmissionResult(session = session)
  }
}
