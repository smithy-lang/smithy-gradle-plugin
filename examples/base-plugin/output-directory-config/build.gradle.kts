// This example writes Smithy build artifacts to a specified directory.

plugins {
    id("java-library")
    id("smithy-base").version("0.7.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}
