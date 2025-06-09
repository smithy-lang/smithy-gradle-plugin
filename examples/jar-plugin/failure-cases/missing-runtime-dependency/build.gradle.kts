// This example creates a projected version of the model, but because the
// projected model references traits from another package and that package
// is not part of the runtime dependencies, the build will fail when the
// plugin validates the JAR with Smithy model discovery.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("1.3.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    smithyBuild("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")

    // This dependency is used in the projected model, so it's required here too.
    // This should fail to build since this is missing.
    //implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    sourceProjection.set("foo")
    format.set(false)
}
