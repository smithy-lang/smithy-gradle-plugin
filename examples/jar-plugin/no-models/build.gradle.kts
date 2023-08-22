// This example is an integration test to ensure that projects with no models do not fail.

plugins {
    id("java-library")
    id("smithy-jar").version("0.8.0")
}

group = "software.amazon.smithy"
version = "9.9.9"

repositories {
    mavenLocal()
    mavenCentral()
}
