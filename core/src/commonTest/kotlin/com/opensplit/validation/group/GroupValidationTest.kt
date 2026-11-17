package com.opensplit.validation.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupValidationTest {
  @Test
  fun acceptsValidGroupName() {
    val result = GroupValidation.validateCreateGroup("Roommates")

    assertTrue(result.isValid)
    assertTrue(result.errors.isEmpty())
  }

  @Test
  fun rejectsBlankGroupName() {
    val result = GroupValidation.validateCreateGroup("   ")

    assertFalse(result.isValid)
    assertEquals("Group name is required", result.errors["name"])
  }

  @Test
  fun rejectsGroupNameLongerThan255Characters() {
    val result = GroupValidation.validateCreateGroup("a".repeat(256))

    assertFalse(result.isValid)
    assertEquals("Group name is too long", result.errors["name"])
  }

  @Test
  fun acceptsGroupNameOfExactly255Characters() {
    val result = GroupValidation.validateCreateGroup("a".repeat(255))

    assertTrue(result.isValid)
  }

  @Test
  fun acceptsNonBlankInviteCode() {
    val result = GroupValidation.validateJoinGroup("ABC123")

    assertTrue(result.isValid)
    assertTrue(result.errors.isEmpty())
  }

  @Test
  fun rejectsBlankInviteCode() {
    val result = GroupValidation.validateJoinGroup("   ")

    assertFalse(result.isValid)
    assertEquals("Invite code is required", result.errors["inviteCode"])
  }
}
