// This project adds smithy models to a JAR created by a Kotlin project

plugins {
    kotlin("jvm") version "1.9.23"
    id("software.amazon.smithy.gradle.smithy-jar").version("1.4.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
