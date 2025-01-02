description = "Use existing, hand-written Java trait definition alongside generated trait definitions."

plugins {
    id("software.amazon.smithy.gradle.smithy-trait-package").version("1.2.0")
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
