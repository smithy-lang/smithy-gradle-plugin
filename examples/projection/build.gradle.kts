// This example places a projected version of the model into the JAR.

plugins {
    java
    id("software.amazon.smithy").version("0.3.0")
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        // This dependency is required to build the model.
        classpath("software.amazon.smithy:smithy-aws-traits:0.9.0")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.9.0")

    // This dependency is used in the projected model, so it's requird here too.
    implementation("software.amazon.smithy:smithy-aws-traits:0.9.0")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "foo"
}
