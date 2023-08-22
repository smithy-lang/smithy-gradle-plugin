// This example writes Smithy build artifacts to a specified directory and
// places a projected version of the model into the JAR.

plugins {
    id("java-library")
    id("smithy-base").version("0.8.0")
}


repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // This dependency is used in the projected model, so it's requird here too.
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    sourceProjection.set("foo")

    // This could also be set to another directory outside the project's buildDir entirely.
    outputDirectory.set(file(project.getBuildDir().toPath().resolve("nested-output-directory").toFile()))
}
