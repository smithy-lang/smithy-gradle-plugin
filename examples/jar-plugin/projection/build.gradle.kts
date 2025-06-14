// This example places a projected version of the model into the JAR.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("1.3.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    sourceProjection.set("foo")
}
