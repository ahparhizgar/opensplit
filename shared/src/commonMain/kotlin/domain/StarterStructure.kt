package domain

data class StarterStructure(
    val modules: List<String>,
) {
    fun supportsSharedFoundation(): Boolean = modules.containsAll(listOf("client", "server", "shared"))
}
