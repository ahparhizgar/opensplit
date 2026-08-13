import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask

plugins {
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidMultiplatformLibrary) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.kotlinJvm) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.ktor) apply false
  alias(libs.plugins.kotest) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.ktfmt)
  alias(libs.plugins.androidx.room) apply false
  alias(libs.plugins.kover)
}

dependencies {
  kover(project(":app:shared"))
  kover(project(":server"))
  kover(project(":core"))
}

kover {
  reports {
    total {
      xml { onCheck = true }
      html { onCheck = true }
    }
    filters { includes { classes("com.opensplit.*") } }
    verify { rule { bound { minValue = 0 } } }
  }
}

tasks.register<KtfmtFormatTask>("ktfmtPrecommit") {
  source = project.fileTree(rootDir)
  include("**/*.kt", "**/*.kts")
}
