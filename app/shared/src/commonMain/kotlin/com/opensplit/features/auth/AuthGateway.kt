package com.opensplit.features.auth

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import com.opensplit.remote.RemoteException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

data class AuthSubmissionResult(
    val session: AuthSessionState,
    val householdContext: HouseholdContextState,
)

interface AuthGateway {
  suspend fun signUp(email: String, password: String): AuthSubmissionResult

  suspend fun signIn(email: String, password: String): AuthSubmissionResult
}

class KtorAuthGateway(
    private val client: HttpClient,
) : AuthGateway {
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
    return try {
      val response =
          client.post("path") {
            contentType(ContentType.Application.Json)
            setBody(request)
          }

      if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.Created) {
        val error = runCatching { response.body<ErrorResponse>() }.getOrNull()
        throw RemoteException(
            fieldErrors = error?.errors ?: emptyMap(),
            generalError = error?.errors?.values?.firstOrNull() ?: "Authentication failed",
        )
      }

      val session = response.body<AuthSessionState>()
      val householdContext =
          client
              .get("household-context") { header("Authorization", "Bearer ${session.accessToken}") }
              .body<HouseholdContextState>()
      AuthSubmissionResult(session = session, householdContext = householdContext)
    } catch (e: RemoteException) {
      throw e
    } catch (e: Throwable) {
      // Transport-level error (network, timeouts, etc.). Convert to RemoteException so
      // higher layers can display a user-friendly message.
      throw RemoteException(generalError = e.message ?: "Network error")
    }
  }
}
