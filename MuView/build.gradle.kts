plugins {
    kotlin("jvm")
    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.kotlin.serialization)
}

group = "me.mucloud"
version = "0-beta1"

application {
    mainClass = "me.mucloud.application.mk.serverlauncher.muview.ApplicationKt"

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

tasks{
    val shell = if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        listOf("cmd", "/c")
    } else {
        listOf("bash", "-l", "-c")
    }

    val installMuView = register<Exec>("installMuView"){
        description = "Install MuView NPM Environment."
        commandLine = shell + "npm i"
        workingDir = projectDir.resolve("view")
        standardOutput = System.out
    }

    val buildMuView = register<Exec>("buildMuView"){
        mustRunAfter(installMuView)
        description = "Build MuView by NPM Builder."
        commandLine = shell + "npm run build"
        workingDir = projectDir.resolve("view")
        standardOutput = System.out
    }

    shadowJar{
        mustRunAfter(buildMuView)
        isZip64 = true
    }

    buildFatJar{
        dependsOn(installMuView, buildMuView)
    }

    build{
        dependsOn(installMuView, buildMuView, shadowJar)
    }
}
