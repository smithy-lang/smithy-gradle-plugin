// This example builds the model and places it in the JAR.

plugins {
    java
    id("software.amazon.smithy").version("0.2.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.5.4")
    implementation("software.amazon.smithy:smithy-aws-traits:0.5.4")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    // Uncomment this to use a custom projection when building the JAR.
    // projection = "foo"
}

// Uncomment to disable creating a JAR.
//tasks["jar"].enabled = false
