import org.gradle.kotlin.dsl.testImplementation

plugins {
    kotlin("jvm") version libs.versions.kotlin.language
    kotlin("plugin.spring") version libs.versions.kotlin.language
    kotlin("plugin.jpa") version libs.versions.kotlin.language
    alias(libs.plugins.ktlint)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "de.zyweck.mealplanner"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.bundles.securedSpringBootWebApp)

    runtimeOnly(libs.postgres)

    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.springBootTesting)
    testImplementation(libs.bundles.testContainers)

    testRuntimeOnly(libs.junit.platform.launcher)

    developmentOnly(libs.spring.boot.docker.compose)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
