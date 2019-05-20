// This example attempts to use an invalid projection. The build will fail.

plugins {
    java
    id("software.amazon.smithy").version("0.0.1")
}

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
