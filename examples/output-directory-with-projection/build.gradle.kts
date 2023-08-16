// This example writes Smithy build artifacts to a specified directory and
// places a projected version of the model into the JAR.

plugins {
    id("software.amazon.smithy").version("0.7.0")
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        // This dependency is required to build the model.
        classpath("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")

    // This dependency is used in the projected model, so it's requird here too.
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "foo"
    // This could also be set to another directory outside the project's buildDir entirely.
    outputDirectory = file(project.getBuildDir().toPath().resolve("nested-output-directory").toFile())
}
