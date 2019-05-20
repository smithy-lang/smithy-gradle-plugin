// This example builds the model and places it in the JAR.

plugins {
    java
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath("software.amazon.smithy:smithy-gradle-plugin:0.0.1")
    }
}

apply(plugin = "software.amazon.smithy")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.4.1")
    implementation("software.amazon.smithy:smithy-aws-traits:0.4.1")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    // Uncomment this to use a custom projection when building the JAR.
    // projection = "foo"
}

// Uncomment to disable creating a JAR.
//tasks["jar"].enabled = false
