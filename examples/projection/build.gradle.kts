// This example places a projected version of the model into the JAR.

plugins {
    java
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath("software.amazon.smithy:smithy-gradle-plugin:0.0.1")

        // This dependency is required to build the model.
        classpath("software.amazon.smithy:smithy-aws-traits:0.4.1")
    }
}

apply(plugin = "software.amazon.smithy")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.4.1")

    // This dependency is used in the projected model, so it's requird here too.
    implementation("software.amazon.smithy:smithy-aws-traits:0.4.1")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "foo"
}
