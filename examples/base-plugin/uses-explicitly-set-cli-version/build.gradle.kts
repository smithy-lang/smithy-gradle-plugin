// This example is an integration test to ensure that the smithy-cli version can be
// found by scanning buildScript dependencies.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-base").version("0.10.1")
}

dependencies {
    smithyCli("software.amazon.smithy:smithy-cli:1.45.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
