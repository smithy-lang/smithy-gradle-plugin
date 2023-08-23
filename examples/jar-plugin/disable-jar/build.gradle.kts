// This example builds Smithy models but does not create a JAR.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("0.8.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}

tasks["jar"].enabled = false
