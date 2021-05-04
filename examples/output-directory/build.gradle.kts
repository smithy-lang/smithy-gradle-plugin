// This example writes Smithy build artifacts to a specified directory.

plugins {
    id("software.amazon.smithy").version("0.5.3")
}

buildscript {
    repositories {
        mavenLocal()
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
    // This could also be set to another directory outside the project's buildDir entirely.
    outputDirectory = file(project.getBuildDir().toPath().resolve("nested-output-directory").toFile())
}
