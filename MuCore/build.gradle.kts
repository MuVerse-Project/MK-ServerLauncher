plugins{
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.kotlin.serialization)
}

group = "me.mucloud"
version = "1.0"

dependencies {
    implementation(kotlin("reflect"))

    implementation(libs.bundles.gson.pack)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.netty.all)
    implementation(libs.okHttp)
    implementation(libs.nightconfig)
    implementation(libs.slf4jAPI)

    testImplementation(libs.kotlin.testJunit)
}

tasks.test {
    useJUnitPlatform()
}