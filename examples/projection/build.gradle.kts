// This example places a projected version of the model into the JAR.

plugins {
    id("software.amazon.smithy").version("0.4.2")
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        // This dependency is required to build the model.
        classpath("software.amazon.smithy:smithy-aws-traits:0.9.7")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.9.7")

    // This dependency is used in the projected model, so it's requird here too.
    implementation("software.amazon.smithy:smithy-aws-traits:0.9.7")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "foo"
}
