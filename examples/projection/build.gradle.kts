// This example places a projected version of the model into the JAR.

plugins {
    id("software.amazon.smithy").version("0.5.3")
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        // This dependency is required to build the model.
        classpath("software.amazon.smithy:smithy-validation-model:[1.0, 2.0[")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "foo"
}
