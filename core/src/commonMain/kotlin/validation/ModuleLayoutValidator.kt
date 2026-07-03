package validation

object ModuleLayoutValidator {
  fun isValid(modules: List<String>): Boolean =
      modules.containsAll(listOf("client", "server", "shared"))
}
