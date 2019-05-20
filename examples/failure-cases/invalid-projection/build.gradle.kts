// This example attempts to use an invalid projection. The build will fail.

plugins {
    java
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath("software.amazon.smithy:smithy-gradle-plugin:0.0.1")
    }
}

apply(plugin = "software.amazon.smithy")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.4.1")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "invalid"
}
