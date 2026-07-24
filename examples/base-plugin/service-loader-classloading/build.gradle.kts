// This example verifies code loaded by the real Smithy CLI can use default ServiceLoader discovery.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-base").version("1.4.0")
}

group = "software.amazon.smithy"
version = "9.9.9"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    smithyBuild(project(":service-loader-plugin"))
}

smithy {
    format.set(false)
}
