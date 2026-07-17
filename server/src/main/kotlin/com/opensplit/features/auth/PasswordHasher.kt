package com.opensplit.features.auth

import at.favre.lib.crypto.bcrypt.BCrypt

interface PasswordHasher {
  fun hash(password: String): String

  fun verify(password: String, hash: String): Boolean
}

class BcryptPasswordHasher(private val cost: Int) : PasswordHasher {
  override fun hash(password: String): String =
      BCrypt.withDefaults().hashToString(cost, password.toCharArray())

  override fun verify(password: String, hash: String): Boolean =
      BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}
