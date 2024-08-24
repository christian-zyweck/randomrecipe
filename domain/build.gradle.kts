plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

group = "de.zyweck.mealplanner"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.bundles.testing)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
