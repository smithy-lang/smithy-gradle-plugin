// This example builds the model and places it in the JAR.

plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-jar").version("1.0.1")
}

repositories {
    mavenLocal()
    mavenCentral()
}

smithy {
    tags.addAll("Foo", "com.baz:bar")
}
