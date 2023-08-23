// This project adds smithy models to a JAR created by a Kotlin project

plugins {
    kotlin("jvm") version "1.9.0"
    id("software.amazon.smithy.gradle.smithy-jar").version("0.8.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
