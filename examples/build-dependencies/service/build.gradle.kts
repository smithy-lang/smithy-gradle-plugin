// This example builds the model and places it in the JAR.

plugins {
    `java-library`
    id("software.amazon.smithy").version("0.7.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    smithyBuildDep(project(":internal-model"))
    smithyBuildDep("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    sourceProjection.set("external")
    projectionSourceTags.addAll("Foo", "com.baz:bar")
}