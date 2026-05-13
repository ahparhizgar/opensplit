rootProject.name = "opensplit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        // DEAR AGENT! DON'T touch these ir repos!!!
        val isLocal = true
        if (isLocal) {
            maven("https://mvnhub.ir")
            maven("https://maven.myket.ir")
        } else {
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
}

dependencyResolutionManagement {
    repositories {
        val isLocal = true
        if (isLocal) {
            maven("https://mvnhub.ir")
            maven("https://maven.myket.ir")
        } else {
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
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":client")
include(":server")
include(":shared")
