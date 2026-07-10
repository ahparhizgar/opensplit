package com.opensplit.fake

import com.opensplit.dto.auth.AuthSessionState
import com.opensplit.features.auth.AuthGateway
import com.opensplit.features.auth.AuthSubmissionResult
import com.opensplit.util.FakeService

class FakeAuthGateway : AuthGateway, FakeService {
  override var errorToThrow: Exception? = null

  override suspend fun signUp(email: String, password: String): AuthSubmissionResult = fakeApiCall {
    AuthSubmissionResult(
        session =
            AuthSessionState(userId = "user-1", email = email, accessToken = "token-user-1-$email"),
    )
  }

  override suspend fun signIn(email: String, password: String): AuthSubmissionResult = fakeApiCall {
    signUp(email, password)
  }
}
