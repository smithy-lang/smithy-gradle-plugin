// This example is an integration test to ensure that projects with no models do not fail.

plugins {
    `java-library`
    id("software.amazon.smithy").version("0.7.0")
}

group = "software.amazon.smithy"
version = "9.9.9"

repositories {
    mavenLocal()
    mavenCentral()
}
