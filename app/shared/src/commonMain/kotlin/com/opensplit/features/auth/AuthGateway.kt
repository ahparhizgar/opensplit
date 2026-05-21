package com.opensplit.features.auth

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.dto.auth.SignInRequest
import com.opensplit.dto.auth.SignUpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

data class AuthSubmissionResult(
    val session: AuthSessionState,
    val householdContext: HouseholdContextState,
)

data class AuthRemoteException(
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
) : RuntimeException(generalError ?: "Authentication request failed")

interface AuthGateway {
    suspend fun signUp(email: String, password: String): AuthSubmissionResult
    suspend fun signIn(email: String, password: String): AuthSubmissionResult
}

class KtorAuthGateway(
    private val client: HttpClient,
    private val baseUrl: String,
) : AuthGateway {
    override suspend fun signUp(email: String, password: String): AuthSubmissionResult = submit(
        path = "/auth/sign-up",
        request = SignUpRequest(email = email, password = password),
    )

    override suspend fun signIn(email: String, password: String): AuthSubmissionResult = submit(
        path = "/auth/sign-in",
        request = SignInRequest(email = email, password = password),
    )

    private suspend fun submit(path: String, request: Any): AuthSubmissionResult {
        return try {
            val response = client.post("$baseUrl$path") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.Created) {
                val error = runCatching { response.body<ErrorResponse>() }.getOrNull()
                throw AuthRemoteException(
                    fieldErrors = error?.errors ?: emptyMap(),
                    generalError = error?.errors?.values?.firstOrNull() ?: "Authentication failed",
                )
            }

            val session = response.body<AuthSessionState>()
            val householdContext = client.get("$baseUrl/household-context") {
                header("Authorization", "Bearer ${session.accessToken}")
            }.body<HouseholdContextState>()
            AuthSubmissionResult(session = session, householdContext = householdContext)
        } catch (e: AuthRemoteException) {
            throw e
        } catch (e: Throwable) {
            // Transport-level error (network, timeouts, etc.). Convert to AuthRemoteException so
            // higher layers can display a user-friendly message.
            throw AuthRemoteException(generalError = e.message ?: "Network error")
        }
    }
}

fun createAuthGateway(): AuthGateway = KtorAuthGateway(createAuthHttpClient(), "http://127.0.0.1:8080")

expect fun createAuthHttpClient(): HttpClient
