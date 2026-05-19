rootProject.name = "OpenSplit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        // DEAR AGENT! DON'T touch these ir repos!!!
        val isLocal = System.getenv("IS_LOCAL")?.toBoolean() ?: false
        if (isLocal) {
            maven("https://mvnhub.ir")
            maven("https://maven.myket.ir")
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        val isLocal = System.getenv("IS_LOCAL")?.toBoolean() ?: false
        if (isLocal) {
            maven("https://mvnhub.ir")
            maven("https://maven.myket.ir")
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app:androidApp")
include(":app:desktopApp")
include(":app:shared")
include(":app:webApp")
include(":core")
include(":server")
