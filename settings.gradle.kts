@file:Suppress("LocalVariableName")

rootProject.name = "ktor-persistence-demo"

pluginManagement {
    val kotlin_version: String by settings
    plugins {
        kotlin("jvm") version kotlin_version
        id("com.github.johnrengelman.shadow") version "7.0.0"
        id("com.github.ben-manes.versions") version "0.38.0"
        id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
        id("com.adarshr.test-logger") version "3.0.0"
    }
}
