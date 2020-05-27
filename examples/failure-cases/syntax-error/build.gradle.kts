// This example fails to build due to a syntax error.

plugins {
    id("software.amazon.smithy").version("0.5.1")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}
