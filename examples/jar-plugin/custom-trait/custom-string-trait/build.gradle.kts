// This package defines a custom trait for use in other models
plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("1.4.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}
