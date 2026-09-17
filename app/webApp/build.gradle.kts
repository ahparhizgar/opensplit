import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.ktfmt)
}

kotlin {
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.app.shared)
      implementation(libs.decompose)
      implementation(libs.wrappers.browser)
      implementation(libs.koin.core)

      implementation(libs.compose.ui)
    }
  }
}
