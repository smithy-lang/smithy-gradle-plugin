// This example fails to build due to a syntax error.

plugins {
    java
    id("software.amazon.smithy").version("0.3.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.9.0")
}
