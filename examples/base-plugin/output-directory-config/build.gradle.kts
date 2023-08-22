// This example writes Smithy build artifacts to a specified directory. The
// directory is specified in the smithy-build.json file rather than in the
// gradle build files.

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
