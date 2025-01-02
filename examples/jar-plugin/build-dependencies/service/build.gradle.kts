// This example builds the model and places it in the JAR.

plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-jar").version("1.2.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    smithyBuild(project(":internal-model"))
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    sourceProjection.set("external")
    projectionSourceTags.addAll("Foo", "com.baz:bar")
}
