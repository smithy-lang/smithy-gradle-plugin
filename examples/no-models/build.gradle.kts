// This example is an integration test to ensure that projects with no models do not fail.

plugins {
    id("software.amazon.smithy").version("0.5.2")
}

group = "software.amazon.smithy"
version = "9.9.9"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}
