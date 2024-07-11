description = "Use existing"

plugins {
    id("software.amazon.smithy.gradle.smithy-trait-package") version "1.0.0"
}

group = "software.amazon.smithy"
version = "9.9.9"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}
