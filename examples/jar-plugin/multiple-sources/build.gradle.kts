// This example pulls Smithy models from the following locations:
// - model/
// - src/main/smithy/
// - src/main/resources/META-INF/smithy

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("1.4.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

