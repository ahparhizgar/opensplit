package com.opensplit

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.dto.auth.HouseholdContextState
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.AuthSubmissionResult

class FakeAuthGateway : AuthGateway {
    var signUpCalls = 0

    override suspend fun signUp(email: String, password: String): AuthSubmissionResult {
        signUpCalls++
        return AuthSubmissionResult(
            session = AuthSessionState(
                userId = "user-1",
                email = email,
                accessToken = "token-user-1-$email"
            ),
            householdContext = HouseholdContextState(
                authenticated = true,
                email = email,
                message = "Authenticated household context",
            ),
        )
    }

    override suspend fun signIn(email: String, password: String): AuthSubmissionResult =
        signUp(email, password)
}