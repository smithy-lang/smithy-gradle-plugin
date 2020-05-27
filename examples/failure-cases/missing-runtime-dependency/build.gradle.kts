// This example creates a projected version of the model, but because the
// projected model references traits from another package and that package
// is not part of the runtime dependencies, the build will fail when the
// plugin validates the JAR with Smithy model discovery.

plugins {
    id("software.amazon.smithy").version("0.5.1")
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

    // This dependency is used in the projected model, so it's required here too.
    // This should fail to build since this is missing.
    //implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "foo"
}
