// This example builds the model and places it in the JAR.

plugins {
    id("java-library")
    id("smithy-jar").version("0.7.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    // Uncomment this to use a custom projection when building the JAR.
    // projection = "foo"
}
