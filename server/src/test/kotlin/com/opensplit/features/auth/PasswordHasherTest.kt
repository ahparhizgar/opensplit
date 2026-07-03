package com.opensplit.features.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

  @Test
  fun `hash then verify correct password returns true`() {
    val hasher = BcryptPasswordHasher()
    val hash = hasher.hash("mySecurePassword123!")
    assertTrue(hasher.verify("mySecurePassword123!", hash))
  }

  @Test
  fun `verify wrong password returns false`() {
    val hasher = BcryptPasswordHasher()
    val hash = hasher.hash("correctPassword")
    assertFalse(hasher.verify("wrongPassword", hash))
  }

  @Test
  fun `hash does not contain raw password`() {
    val hasher = BcryptPasswordHasher()
    val hash = hasher.hash("secret123")
    assertFalse(hash.contains("secret123"))
  }

  @Test
  fun `same password produces different hashes each time`() {
    val hasher = BcryptPasswordHasher()
    val hash1 = hasher.hash("samePassword")
    val hash2 = hasher.hash("samePassword")
    assertNotEquals(hash1, hash2)
  }
}
