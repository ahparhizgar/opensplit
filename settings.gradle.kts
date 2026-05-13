import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        val isLocal = true
        if (isLocal) {
            maven("https://mvnhub.ir")
            maven("https://maven.myket.ir")
        } else {
            google()
            gradlePluginPortal()
            mavenCentral()
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val isLocal = true
        if (isLocal) {
            maven("https://mvnhub.ir")
            maven("https://maven.myket.ir")
        } else {
            google()
            mavenCentral()
        }
    }
}

rootProject.name = "opensplit"

include(":client")
include(":server")
include(":shared")
