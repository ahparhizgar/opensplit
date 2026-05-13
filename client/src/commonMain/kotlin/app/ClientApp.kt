package app

import domain.StarterStructure
import support.StarterDefaults

object ClientApp {
    fun starterStructure(): StarterStructure = StarterStructure(StarterDefaults.modules)
}
