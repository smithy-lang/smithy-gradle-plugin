// This example builds Smithy models but does not create a JAR.

plugins {
    java
    id("software.amazon.smithy").version("0.0.1")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.5.0")
}

tasks["jar"].enabled = false
