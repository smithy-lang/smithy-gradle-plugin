// This example builds Smithy models but does not create a JAR.

plugins {
    id("software.amazon.smithy").version("0.4.1")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.9.5")
}

tasks["jar"].enabled = false
