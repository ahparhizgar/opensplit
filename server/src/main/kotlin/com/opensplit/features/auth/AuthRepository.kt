package com.opensplit.features.auth

interface AuthRepository {
  fun findUserByEmail(email: String): AuthUser?

  fun findUserById(userId: String): AuthUser?

  fun createUser(user: AuthUser)
}
