// This example demonstrates the use of the smithy-jar Gradle plugin
// with a project that has multiple subprojects that depend on each other.

plugins {
    `java-library`
}

allprojects {
    group = "software.amazon.smithy.it"
    version = "999.999.999"
}

tasks["jar"].enabled = false

subprojects {
    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }
}
