package domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StarterStructureTest {
  @Test
  fun supportsClientServerSharedLayout() {
    val structure = StarterStructure(listOf("client", "server", "shared"))

    assertTrue(structure.supportsSharedFoundation())
  }

  @Test
  fun rejectsIncompleteLayout() {
    val structure = StarterStructure(listOf("client", "shared"))

    assertFalse(structure.supportsSharedFoundation())
  }
}
