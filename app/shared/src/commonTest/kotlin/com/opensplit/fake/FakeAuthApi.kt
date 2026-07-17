package com.opensplit.fake

import com.opensplit.dto.auth.AuthResult
import com.opensplit.features.auth.AuthApi
import com.opensplit.features.auth.AuthSubmissionResult
import com.opensplit.util.FakeService

class FakeAuthApi : AuthApi, FakeService {
  override var errorToThrow: Exception? = null

  override suspend fun signUp(email: String, password: String): AuthSubmissionResult = fakeApiCall {
    AuthSubmissionResult(
        session = AuthResult(userId = "user-1", email = email, accessToken = "token-user-1-$email"),
    )
  }

  override suspend fun signIn(email: String, password: String): AuthSubmissionResult = fakeApiCall {
    signUp(email, password)
  }
}
