plugins {
    kotlin("jvm")
    application
}

val ktorVersion = "3.1.1"

dependencies {
    implementation(project(":shared"))
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("app.ServerMainKt")
}
