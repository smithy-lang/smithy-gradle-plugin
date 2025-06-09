// This example is an integration test to ensure that projects with no models do not fail.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("1.3.0")
}

group = "software.amazon.smithy"
version = "9.9.9"

repositories {
    mavenLocal()
    mavenCentral()
}
