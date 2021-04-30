@file:Suppress("PropertyName", "SuspiciousCollectionReassignment")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junit_version: String by project
val kotlin_coroutine_version: String by project
val kotlin_serialization_version: String by project
val ktor_version: String by project
val logback_version: String by project
val strikt_version: String by project

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

application {
    @Suppress("DEPRECATION")
    mainClassName = "io.ktor.server.netty.EngineMain"
}

group = "de.stefanbissell.ktor-persistence-demo"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlin_coroutine_version")
    implementation("org.testcontainers:postgresql:1.15.2")
    implementation("com.github.jasync-sql:jasync-postgresql:1.1.7")
    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlin_serialization_version")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlin_coroutine_version")
    testImplementation("io.strikt:strikt-core:$strikt_version")
    testImplementation("io.mockk:mockk:1.11.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
