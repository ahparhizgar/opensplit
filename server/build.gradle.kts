import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.ktor)
  alias(libs.plugins.ktfmt)
}

group = "com.opensplit"

version = "1.0.0"

application {
  mainClass.set("com.opensplit.ApplicationKt")

  val isDevelopment: Boolean = project.providers.gradleProperty("development").isPresent
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
  api(projects.core)
  implementation(libs.logback)
  implementation(libs.ktor.serverCore)
  implementation(libs.ktor.serverNetty)
  implementation(libs.ktor.serverDi)
  implementation(libs.ktor.serverContentNegotiation)
  implementation(libs.ktor.serializationKotlinxJson)
  implementation(libs.ktor.serverAuth)
  implementation(libs.ktor.serverAuthJwt)
  implementation(libs.ktor.jwt)
  implementation(libs.exposed.core)
  implementation(libs.exposed.dao)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.java.time)
  implementation(libs.hikaricp)
  implementation(libs.postgresql)
  implementation(libs.h2)
  implementation(libs.bcrypt)

  testImplementation(libs.ktor.serverTestHost)
  testImplementation(libs.kotlin.testJunit)
  testImplementation(libs.ktor.clientContentNegotiation)
  testImplementation(libs.ktor.serializationKotlinxJson)
  implementation(libs.ktor.clientAuth)
}

tasks.withType(ShadowJar::class.java) {
  archiveFileName.set("server-fat.jar")
  mergeServiceFiles()
  manifest { attributes("Main-Class" to application.mainClass.get()) }
}

val dockerExecutable: String =
    (project.findProperty("dockerCmd") as String?) ?: "/usr/local/bin/docker"

tasks.register<Exec>("dockerComposeUp") {
  group = "docker"
  description =
      "Builds and starts docker-compose (db + server) using repository root docker-compose.yml"
  dependsOn("shadowJar")
  commandLine(
      dockerExecutable,
      "compose",
      "-f",
      project.rootDir.resolve("docker-compose.yml").toString(),
      "up",
      "-d",
      "--build",
  )
}

tasks.register<Exec>("dockerComposeDown") {
  group = "docker"
  description =
      "Stops and removes docker-compose services defined in repository root docker-compose.yml"
  commandLine(
      dockerExecutable,
      "compose",
      "-f",
      project.rootDir.resolve("docker-compose.yml").toString(),
      "down",
  )
}

tasks.register("waitForHealth") {
  group = "verification"
  description = "Polls http://localhost:8080/health until it returns HTTP 200 or times out (2min)"
  dependsOn("dockerComposeUp")
  doLast {
    val url = URI("http://localhost:8080/health").toURL()
    val start = System.currentTimeMillis()
    val timeoutMs = 120_000L
    println("Waiting for server health endpoint at http://localhost:8080/health...")
    while (true) {
      try {
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 2000
        conn.readTimeout = 2000
        conn.requestMethod = "GET"
        val code = conn.responseCode
        if (code == 200) {
          println("Server health OK (HTTP 200)")
          break
        } else {
          println("Health endpoint returned HTTP $code; retrying...")
        }
      } catch (e: Exception) {
        println("Health check failed: ${'$'}{e.message}; retrying...")
      }
      if (System.currentTimeMillis() - start > timeoutMs) {
        throw GradleException(
            "Timed out waiting for server health endpoint after ${'$'}{timeoutMs/1000} seconds"
        )
      }
      Thread.sleep(2000)
    }
  }
}

tasks.register("startBackend") {
  group = "application"
  description =
      "Starts Postgres + server via docker-compose and waits until the server /health is ready"
  dependsOn("waitForHealth")
}
