// This example places a projected version of the model into the JAR.

plugins {
    id("software.amazon.smithy").version("0.4.0")
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath(files("jars/a/a.jar", "jars/b/b.jar", "jars/c/c.jar"))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.9.4")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "foo"
    projectionSourceTags = setOf("X", "Foo")
}
