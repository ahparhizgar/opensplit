package com.opensplit.features.auth

import com.opensplit.dto.auth.AuthResult
import kotlin.uuid.Uuid

class AuthService(
    private val authRepository: AuthRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) {
  fun signUp(email: String, password: String, name: String?): AuthResult {
    val existingUser = authRepository.findUserByEmail(email)
    if (existingUser != null) {
      throw DuplicateEmailException()
    }

    val user =
        AuthUser(
            id = Uuid.random().toString(),
            name = name,
            email = email,
            passwordHash = passwordHasher.hash(password),
        )
    authRepository.createUser(user)
    return user.toSessionState()
  }

  fun signIn(email: String, password: String): AuthResult {
    val user = authRepository.findUserByEmail(email) ?: throw InvalidCredentialsException()
    if (!passwordHasher.verify(password, user.passwordHash)) {
      throw InvalidCredentialsException()
    }
    return user.toSessionState()
  }

  private fun AuthUser.toSessionState(): AuthResult =
      AuthResult(
          userId = id,
          name = name,
          email = email,
          accessToken = jwtService.issue(id, email, name),
      )
}
