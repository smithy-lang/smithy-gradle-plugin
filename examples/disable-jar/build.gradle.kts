// This example builds Smithy models but does not create a JAR.

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

tasks["jar"].enabled = false
