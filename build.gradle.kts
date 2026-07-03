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
}

tasks.register<KtfmtFormatTask>("ktfmtPrecommit") {
  source = project.fileTree(rootDir)
  include("**/*.kt", "**/*.kts")
}
