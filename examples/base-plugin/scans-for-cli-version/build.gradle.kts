// This example is an integration test to ensure that the smithy-cli version can be
// found by scanning buildScript dependencies.

plugins {
    id("java-library")
    id("smithy-base").version("0.7.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
