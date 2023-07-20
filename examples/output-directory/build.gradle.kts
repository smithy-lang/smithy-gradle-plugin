// This example writes Smithy build artifacts to a specified directory.

plugins {
    `java-library`
    id("software.amazon.smithy").version("0.8.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {    // This could also be set to another directory outside the project's buildDir entirely.
    outputDirectory.set(file(project.getBuildDir().toPath().resolve("nested-output-directory").toFile()))
}
