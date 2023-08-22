// This example pulls Smithy models from the following locations:
// - model/
// - src/main/smithy/
// - src/main/resources/META-INF/smithy

plugins {
    id("java-library")
    id("smithy-jar").version("0.8.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

