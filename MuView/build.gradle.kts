plugins {
    kotlin("jvm")
    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.kotlin.serialization)
}

group = "me.mucloud"
version = "0-beta1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    // Development Mode
    val isDevelopment = /*project.ext.has("development")*/ true
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":MuCore"))
    implementation(libs.bundles.ktor.pack)

    implementation("ch.qos.logback:logback-classic:1.5.13")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation(libs.kotlin.test)
}
