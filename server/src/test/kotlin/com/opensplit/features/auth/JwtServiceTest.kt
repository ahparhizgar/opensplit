package com.opensplit.features.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JwtServiceTest {

  @Test
  fun `issue returns a signed token with three dot-separated parts`() {
    val service = JwtService(secret = "test-secret-12345", expiryMs = 86_400_000L)
    val token = service.issue("user-1", "test@example.com")
    val parts = token.split(".")
    assertEquals(3, parts.size, "JWT must have 3 dot-separated parts")
  }

  @Test
  fun `verify returns userId for valid token`() {
    val service = JwtService(secret = "test-secret-12345", expiryMs = 86_400_000L)
    val token = service.issue("user-1", "test@example.com")
    val userId = service.verify(token)
    assertEquals("user-1", userId)
  }

  @Test
  fun `verify returns null for tampered token`() {
    val service = JwtService(secret = "test-secret-12345", expiryMs = 86_400_000L)
    val token = service.issue("user-1", "test@example.com")
    val tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature"
    val userId = service.verify(tampered)
    assertNull(userId)
  }

  @Test
  fun `verify returns null for token signed with different secret`() {
    val issuer = JwtService(secret = "issuer-secret", expiryMs = 86_400_000L)
    val verifier = JwtService(secret = "different-secret", expiryMs = 86_400_000L)
    val token = issuer.issue("user-1", "test@example.com")
    val userId = verifier.verify(token)
    assertNull(userId)
  }

  @Test
  fun `verify returns null for expired token`() {
    val service = JwtService(secret = "test-secret-12345", expiryMs = -1L)
    val token = service.issue("user-1", "test@example.com")
    val userId = service.verify(token)
    assertNull(userId)
  }
}
