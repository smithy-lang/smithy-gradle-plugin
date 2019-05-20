// This example pulls Smithy models from the following locations:
// - model/
// - src/main/smithy/
// - src/main/resources/META-INF/smithy

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
