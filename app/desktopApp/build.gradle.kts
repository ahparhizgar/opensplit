import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.app.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.koin.core)
    implementation(libs.decompose.compose)
}

compose.desktop {
    application {
        mainClass = "com.opensplit.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<JavaExec>("runA") {
    val runTask = tasks.named<JavaExec>("run").get()

    group = "application"
    description = "Run app with profile A"

    classpath = runTask.classpath
    mainClass.set(runTask.mainClass)

    args("--datadir=app-data/A")
}

tasks.register<JavaExec>("runB") {
    val runTask = tasks.named<JavaExec>("run").get()

    group = "application"
    description = "Run app with profile B"

    classpath = runTask.classpath
    mainClass.set(runTask.mainClass)

    args("--datadir=app-data/B")
}
