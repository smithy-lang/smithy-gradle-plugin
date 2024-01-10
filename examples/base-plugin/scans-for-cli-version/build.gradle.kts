// This example is an integration test to ensure that the smithy-cli version can be
// found by scanning buildScript dependencies.

plugins {
    id("software.amazon.smithy.gradle.smithy-base").version("0.9.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
