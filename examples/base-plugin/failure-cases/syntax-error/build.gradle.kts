// This example fails to build due to a syntax error.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-base").version("1.4.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

smithy {
    format.set(false)
}