// This example builds the model and places it in the JAR.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("1.3.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    // Uncomment this to use a custom projection when building the JAR.
    // projection = "foo"
}
